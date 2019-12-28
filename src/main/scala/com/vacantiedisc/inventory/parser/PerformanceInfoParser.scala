package com.vacantiedisc.inventory.parser

import com.vacantiedisc.inventory.models._
import com.vacantiedisc.inventory.util.DateUtils

object PerformanceInfoParser {

  def parseGenre(s: String): Option[Genre] = s.toLowerCase match {
    case "musical" => Some(MUSICAL)
    case "comedy"  => Some(COMEDY)
    case "drama"   => Some(DRAMA)
    case _         => None
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
      case _ => None
    }
  }


}
