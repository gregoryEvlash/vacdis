package com.vacantiedisc.inventory

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import com.vacantiedisc.inventory.config.{ConfigProvider, HttpConf}
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.http.InventoryRoute
import com.vacantiedisc.inventory.service.{CLI, InventoryService, PerformanceService}

import scala.concurrent.Await

object Application extends CLI{

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    val httpConf: HttpConf = ConfigProvider.httpConf

    val db = new DB()
    val performanceService = system.actorOf(Props(classOf[PerformanceService], db))
    val inventoryService = new InventoryService(db, performanceService)

    initData(args)(inventoryService)
    val appliedArgsResult = applyArguments(args)(inventoryService)
    val message = Await.result(appliedArgsResult, timeout)
    println(message)

    val routes = new InventoryRoute(inventoryService).routes
    Http(system).bindAndHandle(routes, httpConf.host, httpConf.port)

  }
}
