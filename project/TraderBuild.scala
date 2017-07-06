import sbt._
import Keys._

object TraderBuild extends Build {

  val scalaTest = "org.scalatest" % "scalatest_2.12" % "3.0.3" % "test"
  val scalaZ = "org.scalaz" % "scalaz-core_2.12" % "7.2.13"
  val commonDependencies = Seq(scalaTest, scalaZ)

  lazy val root = Project(id = "oanda-trader", base = file("."))
    .aggregate(api, instrument, tradingBot)
  lazy val api = Project(id = "oanda-scala-api", base = file("oanda-scala-api"))
    .settings(libraryDependencies ++= commonDependencies)
  lazy val instrument = Project(id = "instruments", base = file("instruments"))
    .dependsOn(api)
    .settings(libraryDependencies ++= commonDependencies)
  lazy val tradingBot = Project(id = "trading-bot", base = file("trading-bot"))
    .dependsOn(api, instrument)
    .settings(libraryDependencies ++= commonDependencies)

}