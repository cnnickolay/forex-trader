package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.instruments.Model.CandleStick

class TrainingModelEstimator(val models: List[TradingModel]) {

  val traders = models.map(new Trader(_))

  def processCandles(candles: List[CandleStick]): Unit = {
    traders.foreach(_.processCandles(candles))
  }

}
