package com.vacantiedisc.inventory.config

import com.typesafe.config.ConfigFactory

object ConfigProvider {

  lazy val conf = ConfigFactory.load()

  lazy val httpConf = HttpConf.apply(conf)
//  lazy val serviceConf = ServiceConf.apply(conf)

}
