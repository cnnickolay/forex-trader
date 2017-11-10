package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.instruments.Model.CandleStick

class TraderManager(val commission: Int, val tradingModel: List[TradingModel]) {

  var candles: Vector[CandleStick] = Vector.empty

  val traders = tradingModel.map(new Trader(commission, _))

  val trader = new Trader(commission, tradingModel.head)

  def processCandles(candle: CandleStick): Unit = {
    candles = candle +: candles
    traders.par.foreach(_.processCandles(candle))
    //    if (candles.length % 1000 == 0) {
    //      println(candles.length)
    //    }
    val window = 24

    if (candles.length > window) {
      val results = traders
        .map(trader => {
          val fromCandle = candles(window)
          val sum = trader.tradesFrom(fromCandle).map(_.profitPips).sum
          (sum, fromCandle, trader)
        })
        .sortBy(_._1)
        .reverse

      val (sum, fromCandle, bestTrader) = results.head
//      println(s"Profit $sum, from ${fromCandle.time}, to ${candle.time}, model ${bestTrader.tradingModel}")

      trader.tradingModel = bestTrader.tradingModel
      trader.processCandles(candle)
      println(trader.statsString)
    }
  }

  /*
    def stats = {
      traders.map(trader => {
        val trades = trader.trades.filter(trade => trade.position.creationOrder.orderCreatedAt.time.toDateTime.getHourOfDay >= 6 && trade.position.creationOrder.orderCreatedAt.time.toDateTime.getHourOfDay <= 14)

      })
    }
  */

  /*
    def x = {
      val window = 100

      if (candles.length > window) {
        val results = traders
          .map(trader => {
            trader.processCandles(candle)
            val fromCandle = candles(window)
            val sum = trader.tradesFrom(fromCandle).map(_.profitPips).sum
            (sum, fromCandle, trader)
          })
          .sortBy(_._1)
          .reverse

        val bestResult = results.head

        println(s"Profit ${bestResult._1}, from ${bestResult._2.time}, to ${candle.time}, model ${bestResult._3.tradingModel}")
      }
    }
  */

}
