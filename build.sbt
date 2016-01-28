//import play.PlayJava

val appName         = "play2.4-rest-security"
val appVersion      = "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc
)
