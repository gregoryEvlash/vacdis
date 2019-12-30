package com.vacantiedisc.inventory.parser

import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.DateUtils

object PerformanceInfoParser extends LazyLogging{

  def parseGenre(s: String): Option[Genre] = s.toLowerCase match {
    case "musical" => Some(MUSICAL)
    case "comedy"  => Some(COMEDY)
    case "drama"   => Some(DRAMA)
    case _         =>
      logger.warn(s"Unable to parse genre $s")
      None
  }

  def parse(row: List[String]): Option[Performance] = {
    row match {
      case title :: date :: genre :: Nil =>
        for {
          dateValue  <- DateUtils.toDateTime(date)
          genreValue <- parseGenre(genre)
        } yield {
          Performance(title, dateValue, genreValue)
        }
      case row =>
        logger.warn(s"Unable to parse row ${row.mkString(",")}")
        None
    }
  }


}
