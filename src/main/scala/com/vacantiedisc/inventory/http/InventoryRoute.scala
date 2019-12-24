package com.vacantiedisc.inventory.http

import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.http.models.BookingRequest
import com.vacantiedisc.inventory.service.InventoryService
import JsonCodecs._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.joda.time.LocalDate

import scala.concurrent.duration._

class InventoryRoute(inventoryService: InventoryService)
  extends LazyLogging
    with HttpHelper
    with Directives
    with FailFastCirceSupport{

//  implicit val timeout: Timeout = Timeout(ConfigProvider.serviceConf.timeoutSec.seconds)
  implicit val timeout: Timeout = 10 seconds

  private val mainPrefix = "inventory"
  private val book = "book"

  def routes: Route =
    pathPrefix(mainPrefix) {
      get {
        pathPrefix(Segment) { date =>
        // TODO with validation
          path(Segment) { fromDate =>
            logger.info(s"Get inventory for $date based on specific date $fromDate")
            handle {
              inventoryService.getInventoryForDate(???, ???)
            }
          } ~ pathEnd {
            logger.info(s"Get inventory for $date")
            handle {
              inventoryService.getInventoryForDate(LocalDate.now(), ???)
          }
          }

        }
      } ~  post {
        pathEndOrSingleSlash {
          entity(as[BookingRequest]) { request =>
          import request._
          logger.info(s"Booking request for $title on $date amount is $amount")
            // TODO with validation
//              withValidateEntity(request) { validatedRequest =>
                handle {
                  inventoryService.bookPerformance(title, date, amount)
                }
//              }
          }
        }
      }
    }

}
