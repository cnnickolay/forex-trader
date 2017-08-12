import sbt._
import Keys._

object TraderBuild extends Build {

  val scalaTest = "org.scalatest" % "scalatest_2.11" % "3.0.3" % "test"
  val scalaZ = "org.scalaz" % "scalaz-core_2.11" % "7.2.13"
  val shapeless = "com.chuusai" % "shapeless_2.11" % "2.3.2"
  val commonDependencies = Seq(scalaTest, scalaZ, shapeless)

  lazy val root = Project(id = "oanda-trader", base = file("."))
    .aggregate(api, instrument, tradingBot)
    .dependsOn(tradingBot)
  lazy val api = Project(id = "oanda-scala-api", base = file("oanda-scala-api"))
    .settings(libraryDependencies ++= commonDependencies)
  lazy val instrument = Project(id = "instruments", base = file("instruments"))
    .dependsOn(api)
    .settings(libraryDependencies ++= commonDependencies)
  lazy val tradingBot = Project(id = "trading-bot", base = file("trading-bot"))
    .dependsOn(api, instrument)
    .settings(libraryDependencies ++= commonDependencies)

}