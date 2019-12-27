package com.vacantiedisc.inventory.config

import com.typesafe.config.Config
import com.vacantiedisc.inventory.models.PerformanceCondition

class ConditionsConf(
                      val sellingStartBeforeDays: Int,
                      val big: PerformanceCondition,
                      val smallRegular: PerformanceCondition,
                      val smallDiscount: PerformanceCondition
                    )

object ConditionsConf {

  private def parseConfig(conf: Config): PerformanceCondition = {
    PerformanceCondition(
      startAfterDays = conf.getInt("startAfterDays"),
      endAfterDays = conf.getInt("endAfterDays"),
      capacity = conf.getInt("capacity"),
      discountPercent = conf.getDouble("discountPercent"),
      dailyAvailability = conf.getInt("dailyAvailability")
    )
  }

  def apply(conf: Config): ConditionsConf = {
    val c = conf.getConfig("conditions")
    new ConditionsConf(
      sellingStartBeforeDays = c.getInt("sellingStartBeforeDays"),
      big                    = parseConfig(c.getConfig("big")),
      smallRegular           = parseConfig(c.getConfig("small.regular")),
      smallDiscount          = parseConfig(c.getConfig("small.discount"))
    )
  }
}