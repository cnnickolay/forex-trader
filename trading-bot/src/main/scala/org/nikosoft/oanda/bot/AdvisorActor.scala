package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.instruments.Model._

class AdvisorActor(chart: Chart) extends Actor {

  def receive: Receive = {
    case candles: Seq[CandleStick] => //candles.foreach(printResult)
    case candle: CandleStick => printResult(candle)
  }

  def printResult(candle: CandleStick): Unit = {
    println(candle)
    chart.indicators.foreach {
      case indicator: MACDCandleCloseIndicator =>
        println(indicator._values.headOption.map(_.histogram))
      case indicator: RSICandleCloseIndicator =>
        println(indicator._values.headOption)
      case indicator: ATRCandleIndicator =>
        println(indicator._values.headOption)
      case _ =>
    }
    println("---------")
  }
}
