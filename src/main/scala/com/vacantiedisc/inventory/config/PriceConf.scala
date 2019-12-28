package com.vacantiedisc.inventory.config

import com.typesafe.config.Config

class PriceConf(
                val comedy: Double,
                val musical: Double,
                val drama: Double
               )

object PriceConf {
  def apply(conf: Config): PriceConf = {
    val c = conf.getConfig("price")
    new PriceConf(
      c.getDouble("comedy"),
      c.getDouble("musical"),
      c.getDouble("drama")
    )
  }
}
