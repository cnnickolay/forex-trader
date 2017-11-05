package org.nikosoft.oanda.bot.scalping.tradingmodels

import org.joda.time.Duration
import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.bot.scalping.TradingModel
import org.nikosoft.oanda.instruments.Model.CandleStick

class BigSMATradingModelWithStopLoss(val commission: Int,
                                     val minTakeProfit: Int,
                                     val stopTradingAfterHours: Int,
                                     val smaRange: Int,
                                     val stopLoss: Int) extends TradingModel {

  override def createOrder(candle: CandleStick) = {
    val takeProfit = (candle.close - candle.sma(smaRange)).toPips
    if (takeProfit.abs >= minTakeProfit) {
      val positionType = if (takeProfit < 0) PositionType.LongPosition else PositionType.ShortPosition

      val takeProfitRate = candle.close + takeProfit.abs.toRate * (if (positionType == PositionType.ShortPosition) -1 else 1)
      val stopLossRate = candle.close + stopLoss.toRate * (if (positionType == PositionType.ShortPosition) 1 else -1)

      val stopLossOrder = StopLossOrder(
        stopLossPrice = stopLossRate,
        positionType = positionType,
        orderCreatedAt = candle
      )

      val takeProfitOrder = TakeProfitOrder(
        takeProfitPrice = takeProfitRate,
        orderCreatedAt = candle,
        positionType = positionType
      )

      Some(MarketOrder(
        positionType = positionType,
        orderCreatedAt = candle,
        chainedOrders = List(stopLossOrder, takeProfitOrder)))
    } else None
  }

  override def closePosition(candle: CandleStick, position: Position) = {
    if (new Duration(position.executionCandle.time, candle.time).getStandardHours >= stopTradingAfterHours) {
      true
    } else false
  }

  override def toString = s"BigSMATradingModelWithStopLoss($commission,$minTakeProfit,$stopTradingAfterHours,$smaRange,$stopLoss)"
}
