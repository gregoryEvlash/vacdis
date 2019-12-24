import com.vacantiedisc.inventory.Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "com.vacantiedisc", scalaVersion := "2.12.9")),
    name := "gregory",
    version := "0.0.1",
    libraryDependencies ++= all

  )
