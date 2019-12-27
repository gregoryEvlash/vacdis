package com.vacantiedisc.inventory.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.vacantiedisc.inventory.db.{DB, TimeTableRow}
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.service.PerformanceService._
import com.vacantiedisc.inventory.util.PerformanceUtils
import com.vacantiedisc.inventory.utils.DateUtils
import org.joda.time.LocalDate

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class InventoryService(db: DB, performanceService: ActorRef) {

  implicit val timeout: Timeout = 10 seconds

  def applyFileData(path: String): Future[Unit] =
    for{
      data <- Future(FileService.parseFile(path))
      _    <- db.putPerformances(data)
      timeTables = data.flatMap(PerformanceUtils.convertToTimeTable)
      _    <- db.insertRows(timeTables)
    } yield ()

  def getInventoryForDate(queryDate: LocalDate, performanceDate: LocalDate): Future[InventoryServiceResponse] =
    getInventory(queryDate, performanceDate)
      .map{ seq => Right(OverviewResponse(seq)) }

  def bookPerformance(title: String, performanceDate: LocalDate, amount: Int): Future[InventoryServiceResponse] = {

    val bookResult = (performanceService ? BookShow(title, performanceDate, amount)).mapTo[PerformanceServiceMessage]

    bookResult.flatMap{
      case ShowSuccessfullyBooked =>
        db.increaseSold(Show(title, performanceDate), amount).map{ _ =>
          Right(BookedResponse(title, performanceDate, amount))
        }
      case other                  =>
        Future(Left(TicketsSoldOut(title, performanceDate)))
    }
  }


  def getInventory(queryDate: LocalDate, performanceDate: LocalDate): Future[Seq[InventoryResult]] = {

    val result = for{
      rows  <- db.getShows(performanceDate)
      titles = rows.map(_.title)
      genres <- db.findGenres(titles)
      availability <- askAvailability(titles, performanceDate)
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

  private def askAvailability(titles: Seq[String], performanceDate: LocalDate): Future[PerformanceSoldTodayBatch] =
    (performanceService ? GetPerformanceSoldRequestBatch(titles, performanceDate)).mapTo[PerformanceSoldTodayBatch]

  def buildShowInfo(row: TimeTableRow, genre: Genre, availability: Map[String, Int], queryDate: LocalDate): ShowInfo = {
    import row._
    ShowInfo(
      title = title,
      ticketsLeft = capacity - sold,
      ticketsAvailable = dailyAvailability - availability.getOrElse(title, 0),
      status = deriveShowStatus(row, queryDate),
      price = calculatePrice(genre, discountPercent)
    )
  }

  def calculatePrice(genre: Genre, discountPercent: Double): Double = {
    Genre.getPrice(genre) * (100 - discountPercent) / 100
  }

  def deriveShowStatus(dbValue: TimeTableRow, queryDate: LocalDate): ShowStatus = {
    import dbValue._
    date match {
      case d if d.isBefore(queryDate) => InThePast
      case d if d.isAfter(queryDate) && Math.abs(DateUtils.getDaysGap(d, queryDate)) > Rule.startSellingDaysBefore =>
        SaleNotStarted
      case _ if sold >= capacity => SoldOut
      case _                     => OpenForSale
    }
  }

}
