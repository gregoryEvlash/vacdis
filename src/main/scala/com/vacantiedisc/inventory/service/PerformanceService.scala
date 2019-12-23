package com.vacantiedisc.inventory.service

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

class PerformanceService() extends Actor with LazyLogging{

  override def receive: Receive = {


    ???
  }


}

object PerformanceService {

  sealed trait PerformanceServiceMessage

}
