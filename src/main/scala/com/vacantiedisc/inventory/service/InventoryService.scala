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

  def applyFileData(path: String): Unit = {
    val data = FileService.parseFile(path)
    db.putPerformances(data)
    data.foreach{ performance =>
      db.insertRows(PerformanceUtils.convertToTimeTable(performance))
    }
  }

  def getInventoryForDate(queryDate: LocalDate, performanceDate: LocalDate): Future[InventoryServiceResponse] = {
    Future{
      Right(
        OverviewResponse(getInventory(queryDate, performanceDate))
      )
    }
  }

  def bookPerformance(title: String, performanceDate: LocalDate, amount: Int): Future[InventoryServiceResponse] = {

    performanceService ? BookShow(title, performanceDate)
    ???
  }

  def getInventory(queryDate: LocalDate, performanceDate: LocalDate): Seq[InventoryResult] = {
    val dbRows = db.getShows(performanceDate)
    val titles = dbRows.map(_.title)
    val genres = db.findGenres(titles)

    // todo with a thread safe
    val f = (performanceService ? GetPerformanceSoldRequestBatch(titles, performanceDate)).mapTo[PerformanceSoldTodayBatch]
    val availability = Await.result(f, 5 seconds)

    val result = for{
      row  <- dbRows
      genre <- genres.get(row.title)
    } yield {
      genre -> buildShowInfo(row, genre, availability.sold, queryDate)
    }

    result.groupBy(_._1).map{ case (genre, genreAndInfo) =>
      InventoryResult(genre, genreAndInfo.map(_._2))
    }.toSeq
  }

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
