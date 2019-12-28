package com.vacantiedisc.inventory.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.vacantiedisc.inventory.config.{ConditionsConf, PriceConf}
import com.vacantiedisc.inventory.db.{DB, TimeTableRow}
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.service.PerformanceService._
import com.vacantiedisc.inventory.util.PerformanceUtils
import com.vacantiedisc.inventory.util.DateUtils
import org.joda.time.LocalDate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class InventoryService(db: DB, conditionsConf: ConditionsConf, priceConf: PriceConf, performanceService: ActorRef) {

  implicit val timeout: Timeout = 10.seconds

  def applyFileData(path: String): Future[Unit] =
    for{
      data <- Future(FileService.parseFile(path))
      _    <- db.putPerformances(data)
      timeTables = data.flatMap(PerformanceUtils.convertToTimeTable(_)(conditionsConf))
      _    <- db.insertRows(timeTables)
    } yield ()

  def getInventoryForDate(queryDate: LocalDate, performanceDate: LocalDate): Future[InventoryServiceResponse] =
    getInventory(queryDate, performanceDate)
      .map{ seq => Right(OverviewResponse(seq)) }

  def bookPerformance(title: String, performanceDate: LocalDate, amount: Int): Future[InventoryServiceResponse] = {
    deriveBookingAvailability(title, performanceDate, amount)
      .flatMap{ either =>
        either.fold(
          l => Future.successful(Left(l)),
          r => (performanceService ? BookShow(r.title, performanceDate, amount))
            .mapTo[PerformanceServiceMessage]
            .flatMap(handleBookingResponse)
        )
      }
  }

  private def handleBookingResponse(msg: PerformanceServiceMessage): Future[InventoryServiceResponse] = msg match{
    case ShowSuccessfullyBooked(title, performanceDate, amount) =>
      db.increaseSold(Show(title, performanceDate), amount).map{ _ =>
        Right(BookedResponse(title, performanceDate, amount))
      }
    case other =>
      Future(
        Left(Custom(s"There were internal error $other"))
      )
  }

  protected def deriveBookingAvailability(title: String, performanceDate: LocalDate, amount: Int): Future[Either[InventoryError, TimeTableRow]] = {
    for{
      maybeRow     <- db.findRow(title, performanceDate)
      availability <- askAvailability(title, performanceDate)
    } yield {
      maybeRow match {
        case None    =>
          Left(NotFound(title))
        case Some(p) if deriveShowStatus(p, LocalDate.now) == InThePast =>
          Left(Custom(s"You cant book show that has already passed"))
        case Some(p) if deriveShowStatus(p, LocalDate.now) == SaleNotStarted =>
          Left(NotStarted(title))
        case Some(p) if p.sold >= p.capacity =>
          Left(TicketsSoldOut(title, performanceDate))
        case Some(p) if p.dailyAvailability <= availability.sold =>
          Left(Custom(s"Tickets limit on $title reached for today"))
        case Some(p) if p.dailyAvailability - availability.sold <= amount=>
          Left(Custom(s"You cant by more than ${p.dailyAvailability - availability.sold} tickets today"))
        case Some(p) =>
          Right(p)
      }
    }
  }

  protected def getInventory(queryDate: LocalDate, performanceDate: LocalDate): Future[Seq[InventoryResult]] = {

    val result = for{
      rows  <- db.getShows(performanceDate)
      titles = rows.map(_.title)
      genres <- db.findGenres(titles)
      availability <- askAvailabilityBatch(titles, performanceDate)
    } yield {
      for{
        row   <- rows
        genre <- genres.get(row.title)
      } yield {
        genre -> buildShowInfo(row, genre, availability.sold, queryDate)
      }
    }
    result.map(toInventoryResult)
  }

  private def toInventoryResult(info: Seq[(Genre, ShowInfo)]): Seq[InventoryResult] = {
    info.groupBy(_._1).map{ case (genre, genreAndInfo) =>
      InventoryResult(genre, genreAndInfo.map(_._2))
    }.toSeq
  }

  private def askAvailabilityBatch(titles: Seq[String], performanceDate: LocalDate): Future[PerformanceSoldTodayBatch] =
    (performanceService ? GetPerformanceSoldRequestBatch(titles, performanceDate)).mapTo[PerformanceSoldTodayBatch]

  private def askAvailability(title: String, performanceDate: LocalDate): Future[PerformanceSoldToday] =
    (performanceService ? GetPerformanceSoldRequest(title, performanceDate)).mapTo[PerformanceSoldToday]

  protected def buildShowInfo(row: TimeTableRow, genre: Genre, availability: Map[String, Int], queryDate: LocalDate): ShowInfo = {
    import row._
    ShowInfo(
      title = title,
      ticketsLeft = capacity - sold,
      ticketsAvailable = dailyAvailability - availability.getOrElse(title, 0),
      status = deriveShowStatus(row, queryDate),
      price = calculatePrice(genre, discountPercent)(priceConf)
    )
  }

  protected def calculatePrice(genre: Genre, discountPercent: Double)(priceConf: PriceConf): Double = {
    val basicPrice = genre match {
      case MUSICAL => priceConf.musical
      case COMEDY  => priceConf.comedy
      case DRAMA   => priceConf.drama
    }

    basicPrice * (100 - discountPercent) / 100
  }

  protected def deriveShowStatus(dbValue: TimeTableRow, queryDate: LocalDate): ShowStatus = {
    import dbValue._
    date match {
      case d if d.isBefore(queryDate) => InThePast
      case d if d.isAfter(queryDate) && DateUtils.getDaysGap(d, queryDate) >
        conditionsConf.sellingStartBeforeDays => SaleNotStarted
      case _ if sold >= capacity      => SoldOut
      case _                          => OpenForSale
    }
  }

}
