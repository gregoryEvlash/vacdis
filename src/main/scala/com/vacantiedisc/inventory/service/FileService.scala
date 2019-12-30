package com.vacantiedisc.inventory.service

import java.io.File

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import com.vacantiedisc.inventory.models.Performance
import com.vacantiedisc.inventory.parser.PerformanceInfoParser

import scala.util.Try

object FileService extends LazyLogging{
  def parseFile(path: String): Seq[Performance] = {
    Try {
      val reader = CSVReader.open(new File(path))
      val data =  reader.all().flatMap(PerformanceInfoParser.parse)
      reader.close()
      data
    }.recover{
      case t =>
      logger.error(s"Unable to parse file ${t.getMessage}")
      Seq.empty[Performance]
    }.get
  }
}