package com.vacantiedisc.inventory

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import com.vacantiedisc.inventory.config.{ConfigProvider, HttpConf}
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.http.InventoryRoute
import com.vacantiedisc.inventory.service.{InventoryService, PerformanceService}
import org.joda.time.LocalDate


object Application {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val path = "/home/gregory/projects/vacdis/src/main/resources/shows.csv"

    val qD = LocalDate.parse("2019-12-24")
    val td = LocalDate.parse("2019-12-25")

    val db = new DB()
    val performanceService = system.actorOf(Props(classOf[PerformanceService], db))
    val inventoryService = new InventoryService(db, performanceService)
    inventoryService.applyFileData(path)
    val result = inventoryService.getInventory(qD, td)
//    println(result.map(_.genre))

    val httpConf: HttpConf = ConfigProvider.httpConf

    val routes = new InventoryRoute(inventoryService).routes
    Http(system).bindAndHandle(routes, httpConf.host, httpConf.port)

  }
}
