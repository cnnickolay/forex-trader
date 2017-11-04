package org.nikosoft.oanda.bot.scalping.logic

import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.instruments.Model.CandleStick

object StopLossLogic {

  def stopLoss(commissionPips: Int, candles: List[CandleStick], position: Position): Option[Trade] =
    position.creationOrder.findStopLossOrder.fold(Option.empty[Trade]) { order =>
      val candle = candles.head
      if (
        (position.positionType == PositionType.LongPosition && order.price >= candle.low) ||
        (position.positionType == PositionType.LongPosition && order.price <= candle.high)
      ) {
        val trade = Trade(
          commissionPips = commissionPips,
          orderClosedAt = candle,
          closedAtPrice = order.price,
          tradeType = TradeType.StopLoss,
          position = position)
        Option(trade)
      } else Option.empty[Trade]
    }

}
