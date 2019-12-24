package com.vacantiedisc.inventory.db

import com.vacantiedisc.inventory.models.{DBSearchResult, Genre, Performance, Show, TimeTable}
import org.joda.time.LocalDate

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DB() {

  private val TIMETABLE_TABLE = mutable.Set.empty[TimeTableRow]

  def findRow(title: String, date: LocalDate): Option[TimeTableRow] = {
    TIMETABLE_TABLE.find{ r =>
      r.title == title && r.date == date
    }
  }

  /**
  *   Represent DB transaction for increment sold tickets
    * @param show - the show which user want to buy
    * @return Ticket already sold
    */
  def increaseSold(show: Show): Option[Int] = {
    import show._
    findRow(show.title, show.date).map{ row =>
      val newAmount = row.sold + 1
      val newRow  = row.copy(sold = newAmount)

      TIMETABLE_TABLE.filterNot(r =>
        r.title == title && r.date == date
      ).add(newRow)

      newAmount
    }
  }

  def insertRows(timetables: Seq[TimeTable]): Unit = {
    timetables.foreach{ tt =>
      import tt._
      val row = TimeTableRow(title, date, capacity, discountPercent, dailyAvailability, 0)
      TIMETABLE_TABLE.add(row)
    }
  }

  def getShows(date: LocalDate): Seq[TimeTableRow] =
    TIMETABLE_TABLE.filter(_.date == date).toSeq // to immutable

  def getShowsF(date: LocalDate): Future[Seq[DBSearchResult]] = Future(
    TIMETABLE_TABLE.filter(_.date == date).map{ row =>
      DBSearchResult(Show(row.title, row.date), row.sold)
    }.toSeq
  )

  def getMaximalCapacity(show: Show): Option[Int] = {
    findRow(show.title, show.date).map(_.capacity)
  }

  def getMaximalAvailability(show: Show): Option[Int] = {
    findRow(show.title, show.date).map(_.dailyAvailability)
  }



/////////////////////////////////


  private val PERFORMANCE_INFO_TABLE = new mutable.HashMap[String, Performance]()

  def putPerformances(batch: Seq[Performance]): Unit =
    batch.foreach(p => PERFORMANCE_INFO_TABLE.put(p.title, p))

  def getPerformances(titles: Seq[String]): Seq[Performance] = {
    PERFORMANCE_INFO_TABLE.filter(x => titles.contains(x._1)).values.toSeq
  }
  def findGenres(titles: Seq[String]): Map[String, Genre] = {
    PERFORMANCE_INFO_TABLE.filter(x => titles.contains(x._1)).values.map { p =>
      p.title -> p.genre
    }.toMap
  }

}
