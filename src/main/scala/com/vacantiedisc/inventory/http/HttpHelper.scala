package com.vacantiedisc.inventory.http

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route
import com.vacantiedisc.inventory.models._
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import JsonCodecs._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait HttpHelper extends LazyLogging {

  type Resp = (StatusCode, String)

  /**
    * Convert response from service to response for answer
    *
    * @param value Response from service
    * @return
    */
  def handle(value: Future[Any]): Route = {
    onComplete(value.mapTo[InventoryServiceResponse]) {
      case Success(v) =>
        logger.info(s"Making response $v")
        complete(
          toResponse(either(v))
        )
      case Failure(ex) =>
        logger.error(ex.toString)
        complete(
          toResponse(error(ex))
        )
    }
  }

  /**
    * Convert service response to status code and message
    *
    * @param value Either error or good response from service
    * @return status code and message
    */
  protected def either(value: InventoryServiceResponse): (StatusCode, String) = {
    value.fold[Resp](
      err => StatusCodes.BadRequest -> err.asJson.noSpaces,
      resp => StatusCodes.OK -> resp.asJson.noSpaces
    )
  }

  /**
    * Handle and log unexpected message
    *
    * @param value anything
    * @return status code and message
    */

  protected def error(value: Any): (StatusCode, String) = {
    logger.warn(s"Unexpected response $value")
    StatusCodes.InternalServerError -> Custom(value.toString).asJson.noSpaces
  }

  /**
    * Convert tuple to response
    *
    * @param tpl code and message
    * @return
    */
  protected def toResponse(tpl: (StatusCode, String)): HttpResponse = {
    HttpResponse(
      status = tpl._1,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), tpl._2)
    )
  }

//  /**
//    * Take entity of T, validate it, and in case of validation errors response,
//    * but in case of valid T its provided next to route
//    *
//    * @param entity Entity of type T
//    * @param f      Function that takes entity and proceed normal flow
//    * @tparam T type of entity
//    * @return
//    */
//
//  def withValidateEntity[T: Validator](entity: T)(f: T => Route): Route = {
//    Validator.validate(entity).fold(nel => {
//      complete(
//        toResponse(
//          either(
//            Left(
//              ErrorResponse(nel.toList)
//            )
//          )
//        )
//      )
//    },
//      f
//    )
//  }

}
