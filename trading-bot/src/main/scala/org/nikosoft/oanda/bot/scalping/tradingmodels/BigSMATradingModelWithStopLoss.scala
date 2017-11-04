package org.nikosoft.oanda.bot.scalping.tradingmodels

import org.joda.time.Duration
import org.nikosoft.oanda.bot.scalping.Model.{LongOrderType, Order, ShortOrderType, _}
import org.nikosoft.oanda.bot.scalping.TradingModel
import org.nikosoft.oanda.instruments.Model

class BigSMATradingModelWithStopLoss(val commission: Int, val minTakeProfit: Int, val stopTradingAfterHours: Int, val smaRange: Int, val stopLoss: Int) extends TradingModel {
  override def openOrder(candles: List[Model.CandleStick]) = {
    val current = candles.head

    val takeProfit = (current.close - current.sma(smaRange)).toPips
    if (takeProfit.abs >= minTakeProfit) {
      val orderType = if (takeProfit < 0) LongOrderType else ShortOrderType

      Some(Order(orderType = orderType,
        commissionPips = commission,
        takeProfit = takeProfit.abs,
        stopLoss = stopLoss,
        createdAtCandle = current,
        orderState = PendingOrder))
    } else None
  }

  override def closeOrder(candles: List[Model.CandleStick], order: Order) = order.boughtAtCandle.fold(Option.empty[Order]) { boughtAt =>
    val candle = candles.head
    if (new Duration(boughtAt.time, candle.time).getStandardHours >= stopTradingAfterHours) {
      Some(order.copy(orderState = ClosedByTimeOrder, closedAtPrice = candle.close, closedAtCandle = Some(candle)))
    } else Option.empty[Order]
  }

  override def toString = s"BigSMATradingModel($commission,$minTakeProfit,$stopTradingAfterHours,$smaRange,$stopLoss)"
}
