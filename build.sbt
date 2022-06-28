import sbt.Keys.libraryDependencies
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "silk",
    idePackagePrefix := Some("jesperan.silk"),
    libraryDependencies += "org.seleniumhq.selenium" % "selenium-java"            % "4.2.1",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19",
    libraryDependencies += "com.pirum" %% "scala-kafka-client-akka" % "2.5.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "1.0.1",
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    libraryDependencies += "commons-io"              % "commons-io"               % "20030203.000550",
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    libraryDependencies += "org.scalatest"          %% "scalatest"                % "3.2.12" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.19" % Test,
    libraryDependencies += "org.scalamock"          %% "scalamock"                % "5.2.0" % Test,
    libraryDependencies  += "org.json4s" %% "json4s-native" % "4.0.5" % Test,
  )
