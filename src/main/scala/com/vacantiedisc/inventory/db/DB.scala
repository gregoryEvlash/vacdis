package com.vacantiedisc.inventory.db

import com.vacantiedisc.inventory.models._
import org.joda.time.LocalDate

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DB() {

  val notFoundException = new Exception("Performance not found")

  private val TIMETABLE_TABLE = mutable.Set.empty[TimeTableRow]

  def findRow(title: String, date: LocalDate): Future[Option[TimeTableRow]] =
    Future(TIMETABLE_TABLE.find { r =>
      r.title == title && r.date == date
    })

  /**
    *   Represent DB transaction for increment sold tickets
    * @param show - the show which user want to buy
    * @return Ticket already sold
    */
  def increaseSold(show: Show, amount: Int = 1): Future[Int] = {
    for {
      maybeRow <- findRow(show.title, show.date)
      newRow = maybeRow.map(row => row.copy(sold = row.sold + amount))
      addedRow <- newRow.fold(Future.failed[TimeTableRow](notFoundException))(
        addRow
      )
    } yield addedRow.sold
  }

  def insertRows(timetables: Seq[TimeTable]): Future[Unit] =
    Future
      .sequence(timetables.map { tt =>
        import tt._
        val row = TimeTableRow(
          title,
          date,
          capacity,
          discountPercent,
          dailyAvailability,
          0
        )
        addRow(row)
      })
      .map(_ => ())

  def getShows(date: LocalDate): Future[Seq[TimeTableRow]] =
    Future(TIMETABLE_TABLE.filter(_.date == date).toSeq)

  def getMaximalCapacity(show: Show): Future[Option[Int]] =
    findRow(show.title, show.date).map(_.map(_.capacity))

  def getMaximalAvailability(show: Show): Future[Option[Int]] =
    findRow(show.title, show.date).map(_.map(_.dailyAvailability))

  private def addRow(row: TimeTableRow): Future[TimeTableRow] = Future {
    TIMETABLE_TABLE
      .find(r => r.title == row.title && r.date == row.date)
      .fold[Unit] { () } { TIMETABLE_TABLE.remove }
    TIMETABLE_TABLE.add(row)
    row
  }

  private val PERFORMANCE_INFO_TABLE =
    new mutable.HashMap[String, Performance]()

  def insertPerformances(batch: Seq[Performance]): Future[Unit] =
    Future(batch.foreach(p => PERFORMANCE_INFO_TABLE.put(p.title, p)))

  def getPerformances(titles: Seq[String]): Future[Seq[Performance]] =
    Future(
      PERFORMANCE_INFO_TABLE.filter(x => titles.contains(x._1)).values.toSeq
    )

  def findGenres(titles: Seq[String]): Future[Map[String, Genre]] =
    Future(
      PERFORMANCE_INFO_TABLE
        .filter(x => titles.contains(x._1))
        .values
        .map { p =>
          p.title -> p.genre
        }
        .toMap
    )

}
