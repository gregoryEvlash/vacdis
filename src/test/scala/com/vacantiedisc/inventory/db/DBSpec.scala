package com.vacantiedisc.inventory.db

import com.vacantiedisc.inventory.TestDataUtil
import com.vacantiedisc.inventory.models.Show
import com.vacantiedisc.inventory.util.PerformanceUtils
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class DBSpec extends WordSpec with Matchers with TestDataUtil{

  private val db = new DB

  private val condition = generateCondition(10)
  private val performance = generatePerformance
  private val timetable = PerformanceUtils.applyRule(performance)(condition)

  await(db.insertRows(timetable))

  private val gaugeShow = timetable.head

  "DB" should {

    "find time tables" in {

      val result = for {
        maybeTimeTable <- db.findRow(performance.title, gaugeShow.date)
        foundSeq       <- db.getShows(gaugeShow.date)
      } yield {
        maybeTimeTable.toSeq should contain theSameElementsAs foundSeq
      }

      await(result)
    }

    "increaseSold on amount" in {
      val result = for {
        before    <- db.findRow(performance.title, gaugeShow.date)
        newAmount <- db.increaseSold(Show(gaugeShow.title, gaugeShow.date), 1)
        after     <- db.findRow(performance.title, gaugeShow.date)
      } yield {
        after.get.sold - before.get.sold shouldBe newAmount
      }

      await(result)
    }

    "increaseSold should fail with not relevant data" in {
      val amount = Random.nextInt(5)
      val result = db.increaseSold(Show(Random.nextString(5), gaugeShow.date), amount)

      intercept[Exception](await(result))
    }

    "search across performance" in {
      val result = for{
        _                 <- db.insertPerformances(Seq(performance))
        foundPerformances <- db.getPerformances(Seq(performance.title))
        foundGenres       <- db.findGenres(Seq(performance.title))
      } yield {
        foundPerformances.headOption.map(_.genre) shouldBe foundGenres.headOption.map(_._2)
      }

      await(result)
    }
  }


}
