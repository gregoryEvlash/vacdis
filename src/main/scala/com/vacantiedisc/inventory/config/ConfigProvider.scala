package com.vacantiedisc.inventory.config

import com.typesafe.config.ConfigFactory

object ConfigProvider {

  private lazy val conf = ConfigFactory.load()

  lazy val httpConf       = HttpConf.apply(conf)
  lazy val conditionsConf = ConditionsConf.apply(conf)
  lazy val priceConf      = PriceConf.apply(conf)

}
