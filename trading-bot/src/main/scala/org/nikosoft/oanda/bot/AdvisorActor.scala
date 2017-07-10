package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.instruments.Model.{CandleStick, Chart, MACDCandleCloseIndicator}

class AdvisorActor(chart: Chart) extends Actor {

  def receive: Receive = {
    case candle: CandleStick =>
      chart.indicators.foreach {
        case indicator: MACDCandleCloseIndicator =>
//          indicator._values.lastOption.map(_.macd).foreach(println)
          println(indicator._values.size)
          println("---------")
        case _ =>
      }
//      println(candle)
  }

}
