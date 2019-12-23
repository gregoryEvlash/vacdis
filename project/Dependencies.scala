package com.vacantiedisc.inventory

import sbt._

object Dependencies {

  val jodaTime = "joda-time" % "joda-time" % "2.10.5"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"

  val cats = "org.typelevel" %% "cats-core" % "2.0.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.0.0"

  val log = Seq("ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  )

  val fs2 = "co.fs2" %% "fs2-core" % "2.1.0"

  val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.6"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.6.1",
    "com.typesafe.akka" %% "akka-http" % "10.1.11"
  )

  val all = Seq(jodaTime, scalatest, cats, catsEffect, fs2, csv) ++ log ++ akka
}
