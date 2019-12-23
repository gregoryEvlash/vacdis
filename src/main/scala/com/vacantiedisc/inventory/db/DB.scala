package com.vacantiedisc.inventory.db

import com.vacantiedisc.inventory.models.{DBSearchResult, Genre, Performance, Show}
import org.joda.time.LocalDate

import scala.collection.mutable

class DB() {

  private val storage = new mutable.HashMap[String, mutable.Map[LocalDate, (Int)]]()

  def incr(show: Show): Option[Int] = {
    import show._
    storage.get(title).flatMap{ shows =>
      val previous = shows.getOrElse(date, 0)
      val result = shows.put(date, previous + 1)
      storage.put(title, shows)
      result
    }
  }

  def initDates(title: String, dates: Seq[LocalDate]): Unit = {
    val shows = new mutable.HashMap[LocalDate, Int]()
    dates.foreach(x => shows.put(x, 0))
    storage.put(title, shows)
  }

  def getShows(date: LocalDate): Seq[DBSearchResult] =
    storage.filter(_._2.contains(date)).map{ case (title, timeTable) =>
      DBSearchResult(Show(title, date), timeTable.getOrElse(date, 0))
    }.toSeq

  def getAll = storage



  private val original = new mutable.HashMap[String, Performance]()

  def putPerformances(batch: Seq[Performance]): Unit =
    batch.foreach(p => original.put(p.title, p))

  def getPerformances(titles: Seq[String]): Seq[Performance] = {
    original.filter(x => titles.contains(x._1)).values.toSeq
  }

}
