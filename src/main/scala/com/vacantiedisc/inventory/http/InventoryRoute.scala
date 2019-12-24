package com.vacantiedisc.inventory.http

import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.http.models.{BookingOverviewRequest, BookingRequest}
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
  private val overview = "overview"
  private val book = "book"

  def routes: Route =
    pathPrefix(mainPrefix) {
      post {
        path(overview){
          entity(as[BookingOverviewRequest]) { request =>
            withValidateEntity[BookingOverviewRequest, LocalDate](request){ validatedRequestDate =>
              pathEnd {
                logger.info(s"Get inventory for $validatedRequestDate")
                handle {
                  inventoryService.getInventoryForDate(LocalDate.now(), validatedRequestDate)
                }
              }
            }
          }
        } ~ path(book) {
          entity(as[BookingRequest]) { request =>
          logger.info(s"Booking request for ${request.title} on ${request.date} amount is ${request.amount}")
            withValidateEntity[BookingRequest, BookingRequest](request) { validatedRequest =>
              import validatedRequest._
                handle {
                  inventoryService.bookPerformance(title, date, amount)
                }
              }
          }
        }
      }
    }

}
