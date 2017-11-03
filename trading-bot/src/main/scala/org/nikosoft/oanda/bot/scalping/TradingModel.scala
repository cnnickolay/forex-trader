package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.instruments.Model.CandleStick

trait TradingModel {

  val commission: Int

  def openOrder(candles: List[CandleStick]): Option[Order]

  def closeOrder(candles: List[CandleStick], order: Order): Option[Order]

}
