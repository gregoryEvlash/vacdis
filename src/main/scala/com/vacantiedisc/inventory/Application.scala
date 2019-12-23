package com.vacantiedisc.inventory

import akka.actor.ActorSystem
import com.vacantiedisc.inventory.db.DB
import com.vacantiedisc.inventory.service.InventoryService
import org.joda.time.LocalDate


object Application {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

//    val path = args.headOption.getOrElse()
    val path = "/home/gregory/projects/vacdis/src/main/resources/shows.csv"

    val qD = LocalDate.parse("2019-12-01")
    val td = LocalDate.parse("2020-09-15")

    val db = new DB()
    val inventoryService = new InventoryService(db)
    inventoryService.applyFileData(path)
    val result = inventoryService.getInventory(qD, td)

    result.mkString("\n")

//    val ddd = DB.getAll
//    println(ddd)
//
//    val httpConf: HttpConf = ConfigProvider.httpConf
//
//    implicit val mat: ActorMaterializer = ActorMaterializer()

//    system.actorOf(Props[TariffDBServiceActor], TariffDBServiceActor.name)
//    system.actorOf(Props[SessionDBServiceActor], SessionDBServiceActor.name)
//    val dbService: ActorRef = system.actorOf(Props[DBService])
//
//    val tariffService: ActorRef = system.actorOf(Props(classOf[TariffServiceActor], dbService))
//
//    val backOfficeService: ActorRef = system.actorOf(
//      Props(classOf[BackOfficeServiceActor], tariffService, dbService)
//    )
//
//    val routes = new BackOfficeServer(backOfficeService).routes
//
//    Http(system).bindAndHandle(routes, httpConf.host, httpConf.port)

  }
}
