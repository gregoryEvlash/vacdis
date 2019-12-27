package com.vacantiedisc.inventory.service

import akka.pattern.pipe
import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.service.PerformanceService._
import com.vacantiedisc.inventory.utils.DateUtils
import org.joda.time.LocalDate

import scala.collection.mutable
import scala.concurrent.Future

class PerformanceService(db: DB) extends Actor with LazyLogging {

  type KEY = String
  private val ledger = new mutable.HashMap[KEY, Int]()

  import context._

  override def receive: Receive = {

    case GetPerformanceSoldRequest(title, performanceDate) =>
      Future {
        val key = buildKey(title, performanceDate)
        val sold = ledger.getOrElse(key, 0)
        PerformanceSoldToday(sold)
      } pipeTo sender

    case ResetAvailability =>
      ledger.mapValues{ _ => 0}

    case BookShow(title, performanceDate, amount) =>
      Future{
        val key  = buildKey(title, performanceDate)
        ledger.put(key, ledger.getOrElse(key, 0) + amount)
        ShowSuccessfullyBooked
      }  pipeTo sender

    case GetPerformanceSoldRequestBatch(titles, date) =>
      val sold = titles.map{t =>
        val key = buildKey(t, date)
        t -> ledger.getOrElse(key, 0)
      }.toMap

      sender ! PerformanceSoldTodayBatch(sold)

  }


  // todo move to helper
  private val delim = "_-_"

  private def buildKey(title: String, performanceDate: LocalDate): String = {
    s"${performanceDate.toString(DateUtils.timeFormat)}$delim$title"
  }

}

object PerformanceService {

  sealed trait PerformanceServiceMessage
  case class GetPerformanceSoldRequest(title: String, performanceDate: LocalDate) extends PerformanceServiceMessage
  case class GetPerformanceSoldRequestBatch(titles: Seq[String], performanceDate: LocalDate) extends PerformanceServiceMessage

  case class BookShow(title: String, performanceDate: LocalDate, amount: Int) extends PerformanceServiceMessage
  case object ShowSuccessfullyBooked extends PerformanceServiceMessage

  case object ResetAvailability extends PerformanceServiceMessage

  case class PerformanceSoldToday(sold: Int) extends PerformanceServiceMessage
  case class PerformanceSoldTodayBatch(sold: Map[String, Int]) extends PerformanceServiceMessage

  case class Error(msg: String) extends PerformanceServiceMessage

}
