package org.nikosoft.oanda.bot.scalping.logic

import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.instruments.Model.CandleStick

object TakeProfitLogic {

  def takeProfit(commissionPips: Int, candles: List[CandleStick], position: Position): Option[Trade] =
    position.creationOrder.findTakeProfitOrder.fold(Option.empty[Trade]) { order =>
      val candle = candles.head
      if (
        (position.positionType == PositionType.LongPosition && order.price <= candle.high) ||
        (position.positionType == PositionType.ShortPosition && order.price >= candle.low)
      ) {
        val trade = Trade(
          commissionPips = commissionPips,
          orderClosedAt = candle,
          closedAtPrice = order.price,
          tradeType = TradeType.TakeProfit,
          position = position)
        Option(trade)
      } else Option.empty[Trade]
    }

}
