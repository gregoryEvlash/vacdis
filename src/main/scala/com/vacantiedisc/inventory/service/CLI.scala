package com.vacantiedisc.inventory.service

import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.util.DateUtils
import org.joda.time.LocalDate
import com.vacantiedisc.inventory.json.response.response._
import io.circe.syntax._

import scala.concurrent.{Await, Future}
import scala.util.Try
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait CLI extends LazyLogging{

  val timeout: FiniteDuration = 5.seconds

  def initData(args: Array[String])(inventoryService: InventoryService): Try[Unit] = Try{
    Await.result(inventoryService.applyFileData(args.head), timeout)
  }.recover{
    case t =>
      logger.error(s"$t")
      logger.info("Please provide correct file")
      System.exit(1)
  }

  protected def getDate(args: Array[String], paramName: String): Option[LocalDate] = Try{
    args.apply(args.indexOf(paramName) + 1)
  }.toOption.flatMap(DateUtils.toDateTime)

  def applyArguments(args: Array[String])(inventoryService: InventoryService): Future[String] = {
    val maybeQueryDate = getDate(args, "query-date")
    val maybePerformanceDate = getDate(args, "performance-date")

    (maybeQueryDate, maybePerformanceDate) match {
      case (None, _)          => Future.successful("Wrong query-date format provided")
      case (_, None)          => Future.successful("Wrong performance-date format provided")
      case (Some(q), Some(p)) => inventoryService.getInventoryForDate(q, p).map(_.fold(
        err  => err.asJson.noSpaces,
        resp => resp.asJson.noSpaces
      ))
    }
  }

}
