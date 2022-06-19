import sbt.Keys.libraryDependencies
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "silk",
    idePackagePrefix := Some("jesperan.silk"),
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-java"            % "4.2.1",
    libraryDependencies += "commons-io"              % "commons-io"               % "20030203.000550",
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    libraryDependencies += "org.scalatest"          %% "scalatest"                % "3.2.12" % Test,
    libraryDependencies += "org.scalamock"          %% "scalamock"                % "5.2.0" % Test,
  )
