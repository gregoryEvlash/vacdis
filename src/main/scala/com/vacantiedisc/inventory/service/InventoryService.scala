package com.vacantiedisc.inventory.service

import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.PerformanceUtils
import com.vacantiedisc.inventory.utils.DateUtils
import org.joda.time.LocalDate

import scala.concurrent.Future

class InventoryService(db: DB) {

  def applyFileData(path: String): Unit = {
    val data = FileService.parseFile(path)
    db.putPerformances(data)
    data.foreach { performance =>
      db.initDates(
        performance.title,
        PerformanceUtils.getSellingDates(performance.date)
      )
    }
  }

  def getInventoryForDate(queryDate: LocalDate, performanceDate: LocalDate): Future[InventoryServiceResponse] = {

    ???
  }

  def bookPerformance(title: String, performanceDate: LocalDate, amount: Int): Future[InventoryServiceResponse] = {

    ???
  }

  def getInventory(queryDate: LocalDate, performanceDate: LocalDate): Seq[InventoryResult] = {
    val dbRows = db.getShows(performanceDate)
    val genres = db
      .getPerformances(dbRows.map(_.show.title))
      .map { p =>
        p.title -> p.genre
      }
      .toMap

    val result = for {
      row <- dbRows
      genre <- genres.get(row.show.title)
    } yield {
      genre -> buildShowInfo(row, genre, queryDate)
    }

    result.groupBy(_._1).map {
      case (genre, x) =>
        InventoryResult(genre, x.map(_._2))
    }.toSeq
  }

  def buildShowInfo(dbValue: DBSearchResult,
                    genre: Genre,
                    queryDate: LocalDate): ShowInfo = {
    import dbValue._
    val rule = PerformanceUtils.deriveRule(show.date, queryDate)
    val capacity = PerformanceUtils.ruleToCapacity(rule)

    val showStatus = show.date match {
      case d if d.isBefore(queryDate) => InThePast
      case d
          if d.isAfter(queryDate) && Math.abs(DateUtils.getDaysGap(d, queryDate)) > Rule.startSellingDaysBefore =>
        SaleNotStarted
      case d if bought >= capacity => SoldOut
      case _                       => OpenForSale
    }

    ShowInfo(
      title = show.title,
      ticketsLeft = capacity - bought,
      ticketsAvailable = 0, // todo
      status = showStatus,
      price = Genre.getPrice(genre)
    )

  }

}
