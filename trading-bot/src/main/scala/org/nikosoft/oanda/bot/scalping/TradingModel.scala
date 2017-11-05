package org.nikosoft.oanda.bot.scalping

import org.nikosoft.oanda.bot.scalping.Model._
import org.nikosoft.oanda.instruments.Model.CandleStick

trait TradingModel {

  val commission: Int

  def createOrder(candle: CandleStick): Option[Order] = None

  def cancelOrder(candle: CandleStick, order: Order): Boolean = false

  def closePosition(candle: CandleStick, position: Position): Boolean = false

}
