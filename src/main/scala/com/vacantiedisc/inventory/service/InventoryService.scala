package com.vacantiedisc.inventory.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.config.{ConditionsConf, PriceConf}
import com.vacantiedisc.inventory.db.{DB, TimeTableRow}
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.service.PerformanceService._
import com.vacantiedisc.inventory.util.PerformanceUtils
import org.joda.time.LocalDate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class InventoryService(db: DB,
                       conditionsConf: ConditionsConf,
                       priceConf: PriceConf,
                       performanceService: ActorRef)
    extends InventoryUtils with LazyLogging {

  implicit val timeout: Timeout = 10.seconds

  def applyFileData(path: String): Future[Unit] =
    for {
      _    <- Future(logger.info(s"Applying data from file $path"))
      data <- Future(FileService.parseFile(path))
      _    <- db.insertPerformances(data)
      timeTables = data.flatMap(
        PerformanceUtils.convertToTimeTable(_)(conditionsConf)
      )
      _ <- db.insertRows(timeTables)
    } yield ()

  def getInventoryForDate(queryDate: LocalDate, performanceDate: LocalDate): Future[InventoryServiceResponse] =
    getInventory(queryDate, performanceDate)
      .map { seq =>
        Right(OverviewResponse(seq))
      }

  def bookPerformance(show: Show, amount: Int): Future[InventoryServiceResponse] = {
    deriveBookingAvailability(
      show.title,
      show.date,
      amount,
      conditionsConf.sellingStartBeforeDays
    ).flatMap { either =>
      either.fold(
        l => Future.successful(Left(l)),
        _ =>
          (performanceService ? BookShow(show, amount))
            .mapTo[PerformanceServiceMessage]
            .flatMap(handleBookingResponse)
      )
    }
  }

  private def handleBookingResponse(msg: PerformanceServiceMessage): Future[InventoryServiceResponse] = msg match {
    case ShowSuccessfullyBooked(show, amount) =>
      db.increaseSold(show, amount).map { _ =>
        Right(BookedResponse(show, amount))
      }
    case other =>
      Future(Left(Custom(s"There were internal error $other")))
  }

  protected def deriveBookingAvailability(
    title: String,
    performanceDate: LocalDate,
    amount: Int,
    sellingStartBeforeDays: Int
  ): Future[Either[InventoryError, TimeTableRow]] = {
    for {
      _            <- Future(logger.info("Evaluate booking availability"))
      maybeRow     <- db.findRow(title, performanceDate)
      availability <- askAvailability(Show(title, performanceDate))
    } yield {
      maybeRow match {
        case None =>
          Left(NotFound(title))
        case Some(p)
            if deriveShowStatus(p, LocalDate.now, sellingStartBeforeDays) == InThePast =>
          Left(Custom(s"You cant book show that has already passed"))
        case Some(p)
            if deriveShowStatus(p, LocalDate.now, sellingStartBeforeDays) == SaleNotStarted =>
          Left(NotStarted(title))
        case Some(p) if p.sold >= p.capacity =>
          Left(TicketsSoldOut(title, performanceDate))
        case Some(p) if p.dailyAvailability <= availability.sold =>
          Left(Custom(s"Tickets limit on $title reached for today"))
        case Some(p) if p.dailyAvailability - availability.sold <= amount =>
          Left(
            Custom(
              s"You cant by more than ${p.dailyAvailability - availability.sold} tickets today"
            )
          )
        case Some(p) =>
          Right(p)
      }
    }
  }

  protected def getInventory(queryDate: LocalDate, performanceDate: LocalDate): Future[Seq[InventoryResult]] = {
    val result = for {
      rows <- db.getShows(performanceDate)
      titles = rows.map(_.title)
      genres <- db.findGenres(titles)
      availability <- askAvailabilityBatch(titles, performanceDate)
    } yield {
      for {
        row <- rows
        genre <- genres.get(row.title)
      } yield {
        genre -> buildShowInfo(
          row,
          genre,
          availability.sold,
          queryDate,
          conditionsConf.sellingStartBeforeDays
        )(priceConf)
      }
    }
    result.map(toInventoryResult)
  }

  private def toInventoryResult(info: Seq[(Genre, ShowInfo)]): Seq[InventoryResult] = {
    info
      .groupBy(_._1)
      .map {
        case (genre, genreAndInfo) =>
          InventoryResult(genre, genreAndInfo.map(_._2).toList)
      }
      .toSeq
  }

  private def askAvailabilityBatch(titles: Seq[String], performanceDate: LocalDate): Future[PerformanceSoldTodayBatch] =
    (performanceService ? GetPerformanceSoldRequestBatch(titles, performanceDate))
      .mapTo[PerformanceSoldTodayBatch]

  private def askAvailability(show: Show): Future[PerformanceSoldToday] =
    (performanceService ? GetPerformanceSoldRequest(show))
      .mapTo[PerformanceSoldToday]

}
