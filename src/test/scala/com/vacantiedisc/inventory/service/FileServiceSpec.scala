package com.vacantiedisc.inventory.service

import org.scalatest.{Matchers, WordSpec}

import scala.util.Random

class FileServiceSpec extends WordSpec with Matchers {

  "FileService" should {

    "return empty if failed" in {
      FileService.parseFile(Random.nextString(10)) shouldBe empty
    }

  }
}
