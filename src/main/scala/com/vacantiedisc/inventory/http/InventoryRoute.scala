package com.vacantiedisc.inventory.http

import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.http.models.{BookingOverviewRequest, BookingRequest}
import com.vacantiedisc.inventory.service.InventoryService
import JsonCodecs._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.joda.time.LocalDate
import FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._

class InventoryRoute(inventoryService: InventoryService)
  extends LazyLogging
    with HttpHelper
    with Directives
{

//  implicit val timeout: Timeout = Timeout(ConfigProvider.serviceConf.timeoutSec.seconds)
  implicit val timeout: Timeout = 10 seconds

  private val mainPrefix = "inventory"
  private val overview = "overview"
  private val book = "book"

  def routes: Route =
    pathPrefix(mainPrefix) {
      post {
        pathPrefix(overview) {
          pathEnd {
            entity(as[BookingOverviewRequest]) { request =>
              withValidateEntity[BookingOverviewRequest, LocalDate](request) { validatedRequestDate =>
                logger.info(s"Get inventory for $validatedRequestDate")
                handle {
                  inventoryService.getInventoryForDate(LocalDate.now(), validatedRequestDate)
                }
              }
            }
          }
        } ~ pathPrefix(book) {
          pathEnd {
            entity(as[BookingRequest]) { payload =>
              logger.info(s"Booking request for ${payload.title} on ${payload.date} amount is ${payload.amount}")
              withValidateEntity[BookingRequest, BookingRequest](payload) { validatedRequest =>
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

}
