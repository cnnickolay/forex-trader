package org.nikosoft.oanda.bot.scalping.tradingmodels

import org.nikosoft.oanda.bot.scalping.Model.PositionType.{LongPosition, ShortPosition}
import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.bot.scalping.TradingModel
import org.nikosoft.oanda.instruments.Model
import org.nikosoft.oanda.instruments.Model.CandleStick

import scalaz.Scalaz._

class SingleCandleTrader(tipLength: Int = 50, pipsToActivate: Int = 250, pipsToTakeProfit: Int = 50, pipsToStopLoss: Option[Int] = None, maxCandlesToGo: Int = 0) extends TradingModel {

  var candlesPassed = 0

  override def createOrder(candle: Model.CandleStick): Option[Order] = {
    val totalMagnitude = (candle.high - candle.low).toPips
    val magnitude = (candle.open - candle.close).toPips
    val positionType = if (magnitude > 0) LongPosition else ShortPosition
    if (totalMagnitude.abs >= pipsToActivate && ((positionType == LongPosition && (candle.close - candle.low).toPips >= tipLength) || (positionType == ShortPosition && (candle.high - candle.close).toPips >= tipLength))) {
      val takeProfit = TakeProfitOrder(candle, (positionType == LongPosition) ? (candle.close + pipsToTakeProfit.toRate) | (candle.close - pipsToTakeProfit.toRate), positionType)
      val stopLoss = pipsToStopLoss.map(value => StopLossOrder(candle, (positionType == LongPosition) ? (candle.close - value.toRate) | (candle.close + value.toRate), positionType))

      Some(
        MarketOrder(
          positionType = positionType,
          orderCreatedAt = candle,
          chainedOrders = List(takeProfit) ++ stopLoss
        )
      )
    } else None
  }

  override def closePosition(candle: CandleStick, position: Position): Boolean = {
    candlesPassed = candlesPassed + 1
    if (candlesPassed > maxCandlesToGo) {
      candlesPassed = 0
      true
    } else false
  }

  override def toString = s"SingleCandleTrader($pipsToActivate, $pipsToTakeProfit, $pipsToStopLoss, $maxCandlesToGo)"
}
