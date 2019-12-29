package com.vacantiedisc.inventory.service

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import com.vacantiedisc.inventory.TestDataUtil
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.models.Show
import com.vacantiedisc.inventory.service.PerformanceService.{BookShow, Error, GetPerformanceSoldRequest, GetPerformanceSoldRequestBatch, PerformanceSoldToday, PerformanceSoldTodayBatch, ResetAvailability, ShowSuccessfullyBooked}
import com.vacantiedisc.inventory.util.PerformanceUtils
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import scala.util.{Random, Success}

class PerformanceServiceSpec
  extends TestKit(ActorSystem("PerformanceServiceSpec"))
  with Matchers
  with WordSpecLike
  with TestDataUtil{
  private val db = new DB

  private val condition = generateCondition(10)
  private val performance = generatePerformance
  private val timetable = PerformanceUtils.applyRule(performance)(condition)
  private val gaugeShow = Show(timetable.head.title, timetable.head.date)
  private val amount = Random.nextInt(5)

  private val performanceService = system.actorOf(Props(classOf[PerformanceService], db))

  "Performance service" must {

    "book show" in {
      val result = performanceService ? BookShow(gaugeShow, amount)

      await(result) shouldBe ShowSuccessfullyBooked(gaugeShow, amount)
    }

    "send back messages PerformanceSoldToday" in {
      val result = performanceService ? GetPerformanceSoldRequest(gaugeShow)

      await(result) shouldBe PerformanceSoldToday(amount)
    }

    "reset availability" in {
      performanceService ! ResetAvailability
      val result = performanceService ? GetPerformanceSoldRequestBatch(Seq(gaugeShow.title), gaugeShow.date)
      await(result) shouldBe PerformanceSoldTodayBatch(Map(gaugeShow.title -> 0))
    }

    "response with error" in {
      val result = performanceService ? Random.nextString(10)
      await(result) shouldBe Error("Unexpected message")
    }

  }

}
