package com.vacantiedisc.inventory.service

import akka.actor.Actor
import akka.pattern.pipe
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.models.Show
import com.vacantiedisc.inventory.service.PerformanceService._
import com.vacantiedisc.inventory.util.PerformanceUtils._
import org.joda.time.{DateTime, LocalDate}

import scala.collection.mutable
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Future, duration}
import scala.util.Try

class PerformanceService(db: DB) extends Actor with LazyLogging {

  type KEY = String
  private val ledger = new mutable.HashMap[KEY, Int]()

  import context._

  override def preRestart(reason: Throwable, message: Option[Any]) {
    self ! ResetAvailability
    system.scheduler.scheduleAtFixedRate(
      timeToMidnight,
      FiniteDuration(24, duration.HOURS),
      self,
      ResetAvailability
    )

    super.preRestart(reason, message)
  }

  override def receive: Receive = {

    case GetPerformanceSoldRequest(show) =>
      Future {
        val key = buildKey(show.title, show.date)
        val sold = ledger.getOrElse(key, 0)
        PerformanceSoldToday(sold)
      } pipeTo sender

    case ResetAvailability =>
      logger.info("Reset daily availability")
      ledger.clear()

    case BookShow(show, amount) =>
      logger.info(s"Receive booking request ${show.title} on ${show.date} for $amount tickets")
      Future{
        Try {
          val key = buildKey(show.title, show.date)
          ledger.put(key, ledger.getOrElse(key, 0) + amount)
          ShowSuccessfullyBooked(show, amount)
        }.recover {
          case t => Error(t.getMessage)
        }.get
      }  pipeTo sender

    case GetPerformanceSoldRequestBatch(titles, date) =>
      val sold = titles.map{t =>
        val key = buildKey(t, date)
        t -> ledger.getOrElse(key, 0)
      }.toMap

      sender ! PerformanceSoldTodayBatch(sold)

    case other =>
      logger.warn(s"Unexpected message $other")
      sender ! Error("Unexpected message")

  }

  private def timeToMidnight: FiniteDuration = {
    val millisGap = DateTime.now().plusDays(1).withTimeAtStartOfDay().getMillis - DateTime.now.getMillis
    Duration.apply(millisGap, duration.MILLISECONDS)
  }

}

object PerformanceService {

  sealed trait PerformanceServiceMessage
  case class GetPerformanceSoldRequest(show: Show) extends PerformanceServiceMessage
  case class GetPerformanceSoldRequestBatch(titles: Seq[String], performanceDate: LocalDate) extends PerformanceServiceMessage

  case class BookShow(show: Show, amount: Int) extends PerformanceServiceMessage
  case class ShowSuccessfullyBooked(show: Show, amount: Int) extends PerformanceServiceMessage

  case object ResetAvailability extends PerformanceServiceMessage

  case class PerformanceSoldToday(sold: Int) extends PerformanceServiceMessage
  case class PerformanceSoldTodayBatch(sold: Map[String, Int]) extends PerformanceServiceMessage

  case class Error(msg: String) extends PerformanceServiceMessage

}
