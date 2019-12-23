package com.vacantiedisc.inventory.config

import com.typesafe.config.Config

class HttpConf(val host: String, val port: Int)

object HttpConf {
  def apply(conf: Config): HttpConf = {
    val c = conf.getConfig("http")
    new HttpConf(
      c.getString("host"),
      c.getInt("port")
    )
  }
}
