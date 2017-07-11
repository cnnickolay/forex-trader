package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, MACDCandleCloseIndicator, RSICandleCloseIndicator}

class AdvisorActor(chart: Chart) extends Actor {

  def receive: Receive = {
    case candle: Seq[CandleStick] =>
      chart.indicators.foreach {
        case indicator: MACDCandleCloseIndicator =>
          indicator._values.map(_.histogram).foreach(println)
          println("---------")
        case indicator: RSICandleCloseIndicator =>
          indicator._values.foreach(println)
          println("---------")
        case _ =>
      }
    case candle: CandleStick =>
      println(candle)
      chart.indicators.foreach {
        case indicator: MACDCandleCloseIndicator =>
          println(indicator._values.headOption.map(_.histogram))
        case indicator: RSICandleCloseIndicator =>
          println(indicator._values.headOption)
        case _ =>
      }
      println("---------")
  }

}
