import Dependencies._

lazy val root = (project in file("."))
  .settings(
      inThisBuild(List(
        scalaVersion := "2.12.4",
        organization := "com.fwsim",
        version := "0.1.0-SNAPSHOT"
      )), 
      name := "fwsim",
      libraryDependencies += scalaTest % Test
  )
