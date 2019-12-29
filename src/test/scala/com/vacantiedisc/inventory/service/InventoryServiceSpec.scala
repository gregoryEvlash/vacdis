package com.vacantiedisc.inventory.service

import akka.actor.{ActorRef, ActorSystem, Props}
import com.vacantiedisc.inventory.TestDataUtil
import com.vacantiedisc.inventory.config.ConfigProvider
import com.vacantiedisc.inventory.db.{DB, TimeTableRow}
import com.vacantiedisc.inventory.util.PerformanceUtils
import org.scalatest.{Matchers, WordSpec, WordSpecLike}
import akka.testkit.{TestKit, TestProbe}
import com.vacantiedisc.inventory.models._
import org.joda.time.LocalDate

class InventoryServiceSpec
  extends TestKit(ActorSystem("PerformanceServiceSpec"))
  with Matchers
  with WordSpecLike with TestDataUtil with InventoryUtils{

  private val db = new DB

  private val condition = generateCondition(10)
  private val performance = generatePerformance
  private val timetable = PerformanceUtils.applyRule(performance)(condition)

  private val gaugeTimeTable = timetable.head
  await(db.insertRows(timetable))
  await(db.insertPerformances(Seq(performance)))

  val performanceService: ActorRef = system.actorOf(Props(classOf[PerformanceService], db))

  val inventoryService = new InventoryService(db, ConfigProvider.conditionsConf, ConfigProvider.priceConf, performanceService)

  "InventoryUtils" should {

    "calculatePrice properly" in {
      val price = derivePrice(performance.genre, ConfigProvider.priceConf)
      val result = calculatePrice(price, 20)

      result shouldBe price*0.8
    }

    "deriveShowStatus properly" in {
      val sellingBefore = 0
      val gaugeRow = await(db.findRow(gaugeTimeTable.title, gaugeTimeTable.date)).get

      deriveShowStatus(gaugeRow, gaugeRow.date.plusDays(1), sellingBefore) shouldBe InThePast
      deriveShowStatus(gaugeRow, gaugeRow.date.minusDays(1), sellingBefore) shouldBe SaleNotStarted
      deriveShowStatus(gaugeRow.copy(sold = gaugeRow.capacity), gaugeRow.date, sellingBefore) shouldBe SoldOut
      deriveShowStatus(gaugeRow, gaugeRow.date, sellingBefore) shouldBe OpenForSale

    }


  }

  "InventoryService" should {

    "getInventoryForDate response with result" in {
      import gaugeTimeTable._
      val result = inventoryService.getInventoryForDate(date.minusDays(1), date)
      val expectedResult = InventoryResult(
        performance.genre,
        List(
          ShowInfo(
            title,
            capacity,
            dailyAvailability,
            OpenForSale,
            calculatePrice(derivePrice(performance.genre, ConfigProvider.priceConf), discountPercent)
          )
        )
      )
      await(result) shouldBe Right(OverviewResponse(Seq(expectedResult)))
    }
  }

}

