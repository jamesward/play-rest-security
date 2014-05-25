import play.PlayJava

val appName         = "play2.3-rest-security"
val appVersion      = "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean
)
