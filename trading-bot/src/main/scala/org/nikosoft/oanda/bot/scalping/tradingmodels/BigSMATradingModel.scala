package org.nikosoft.oanda.bot.scalping.tradingmodels

import org.joda.time.Duration
import org.nikosoft.oanda.bot.scalping.Model.PositionType.PositionType
import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.bot.scalping.TradingModel
import org.nikosoft.oanda.instruments.Model.CandleStick

case class StopLossOnCloseOrder(orderCreatedAt: CandleStick, price: BigDecimal, positionType: PositionType) extends Order {
  val chainedOrders: List[Order] = Nil
}

class BigSMATradingModel(val commission: Int,
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

      val stopLossOrder = StopLossOnCloseOrder(
        price = stopLossRate,
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
    } else {
      position.creationOrder.findOrderByClass(classOf[StopLossOnCloseOrder]).fold(false) { order =>
        if (
          (position.positionType == PositionType.LongPosition && order.stopLossPrice >= candle.close) ||
          (position.positionType == PositionType.ShortPosition && order.stopLossPrice <= candle.close)
        ) {
          true
        } else false
      }
    }
  }

  override def toString = s"BigSMATradingModel($commission,$minTakeProfit,$stopTradingAfterHours,$smaRange,$stopLoss)"
}
