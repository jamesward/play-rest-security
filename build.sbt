lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

name := "play-rest-security"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  evolutions,
  "org.webjars" % "jquery" % "2.2.1"
)

routesGenerator := InjectedRoutesGenerator
