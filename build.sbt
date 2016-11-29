// val akka = "com.typesafe.akka" % "akka-actor" % "2.4.8"

lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "dinningPhilosophers",
    organization := "br.usp.ime.fllsouto",
    sbtVersion := "0.13.12",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.8",
    libraryDependencies += "net.liftweb" %% "lift-json" % "3.0-RC4"

  )