package org.nikosoft.oanda.bot.scalping

import org.joda.time.Duration
import org.nikosoft.oanda.bot.scalping.Model.PositionType.PositionType
import org.nikosoft.oanda.bot.scalping.Model.TradeType.TradeType
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.math.BigDecimal.RoundingMode


object Model {
  val pipsCoef = 100000

  implicit class CandleStickPimped(candleStick: CandleStick) {
    def macdHistogram = candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0))
    def macdHistogramPips = (candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0)) * pipsCoef).toInt
    def sma(precision: Int) = candleStick.indicators.get(s"SMACandleCloseIndicator_$precision").map(_.asInstanceOf[BigDecimal].setScale(5, RoundingMode.HALF_UP).toDouble).getOrElse(0.0)
    def cmo = candleStick.indicators.get("CMOCandleCloseIndicator_8").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
  }

  implicit class BigDecimalPimped(value: BigDecimal) {
    def toPips: Int = (value * pipsCoef).toInt
  }

  implicit class IntegerPimped(value: Int) {
    def toRate: BigDecimal = BigDecimal(value) / pipsCoef
  }

  object PositionType extends Enumeration {
    type PositionType = Value
    val LongPosition, ShortPosition = Value
  }

  abstract class Order() {
    def chainedOrders: List[Order]
    def orderCreatedAt: CandleStick

    def findTakeProfitOrder: Option[TakeProfitOrder] = chainedOrders.find(_.getClass == classOf[TakeProfitOrder]).map(_.asInstanceOf[TakeProfitOrder])
    def findStopLossOrder: Option[StopLossOrder] = chainedOrders.find(_.getClass == classOf[StopLossOrder]).map(_.asInstanceOf[StopLossOrder])
  }
  case class MarketOrder(positionType: PositionType, orderCreatedAt: CandleStick, chainedOrders: List[Order]) extends Order
  case class LimitOrder(price: BigDecimal, positionType: PositionType, orderCreatedAt: CandleStick, chainedOrders: List[Order]) extends Order
  case class StopLossOrder(orderCreatedAt: CandleStick, price: BigDecimal, positionType: PositionType) extends Order {
    val chainedOrders: List[Order] = Nil
  }
  case class TakeProfitOrder(orderCreatedAt: CandleStick, price: BigDecimal, positionType: PositionType) extends Order {
    val chainedOrders: List[Order] = Nil
  }

  case class Position(creationOrder: Order,
                      executionPrice: BigDecimal,
                      executionCandle: CandleStick,
                      positionType: PositionType)

  object TradeType extends Enumeration {
    type TradeType = Value
    val TakeProfit, StopLoss, ManualClose = Value
  }

  case class Trade(commissionPips: Int,
                   orderClosedAt: CandleStick,
                   closedAtPrice: BigDecimal,
                   tradeType: TradeType,
                   position: Position) {
    def profitPips = (position.positionType match {
      case PositionType.LongPosition => closedAtPrice - position.executionPrice
      case PositionType.ShortPosition => position.executionPrice - closedAtPrice
    }).toPips - commissionPips

    def duration = new Duration(position.executionCandle.time, orderClosedAt.time).toStandardHours.toString
  }

}
