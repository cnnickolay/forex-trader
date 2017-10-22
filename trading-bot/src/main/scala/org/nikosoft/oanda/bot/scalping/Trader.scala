package org.nikosoft.oanda.bot.scalping

import org.joda.time.Duration
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scalaz.Scalaz._

object TraderModel {
  val pipsCoef = 100000

  implicit class CandleStickPimped(candleStick: CandleStick) {
    def macdHistogramPips = (candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0)) * pipsCoef).toInt
    def ema20 = candleStick.indicators.get("EMACandleCloseIndicator_20").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema30 = candleStick.indicators.get("EMACandleCloseIndicator_30").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema50 = candleStick.indicators.get("EMACandleCloseIndicator_50").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def cmo = candleStick.indicators.get("CMOCandleCloseIndicator_8").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
  }

  implicit class BigDecimalPimped(value: BigDecimal) {
    def toPips: Int = (value * pipsCoef).toInt
  }

  implicit class IntegerPimped(value: Int) {
    def toRate: BigDecimal = BigDecimal(value) / pipsCoef
  }

  abstract class OrderType
  case object LongOrderType extends OrderType
  case object ShortOrderType extends OrderType

  abstract class OrderState
  case object PendingOrder extends OrderState
  case object ExecutedOrder extends OrderState
  case object TakeProfitOrder extends OrderState
  case object StopLossOrder extends OrderState
  case object CancelledOrder extends OrderState

  case class Order(orderType: OrderType, commissionPips: Int, openAtPrice: BigDecimal, takeProfit: BigDecimal, stopLoss: BigDecimal, createdAtCandle: CandleStick, closedAtPrice: BigDecimal = 0, boughtAtCandle: Option[CandleStick] = None, closedAtCandle: Option[CandleStick] = None, orderState: OrderState = PendingOrder) {
    def profitPips = (orderType match {
      case LongOrderType => closedAtPrice - openAtPrice
      case ShortOrderType => openAtPrice - closedAtPrice
    }).toPips - commissionPips

    def duration = (boughtAtCandle |@| closedAtCandle) { (b, c) =>
      new Duration(b.time, c.time).toStandardMinutes.toString
    }.getOrElse("")
  }

  def calculateProfit(orderType: OrderType, openPrice: BigDecimal, last: CandleStick, first: CandleStick, commission: Int) = {
    if (orderType == LongOrderType) ((last.high - openPrice) * pipsCoef).toInt - commission
    else if (orderType == ShortOrderType) ((openPrice - last.low) * pipsCoef).toInt - commission
    else 0
  }
}

class Trader(val commission: Int = 10, val openOrderOffset: Int = 45, val takeProfit: Int = 50, val stopLoss: Int = 50) {

  import TraderModel._

  var currentOrderOption: Option[Order] = None
  var orders: List[Order] = List.empty

  def processCandles(candles: Seq[CandleStick]): Option[Order] = (candles, currentOrderOption) match {
    case (_ :+ prev :+ current, None) => // order creation
      if (prev.macdHistogramPips < 0 && current.macdHistogramPips > 0) {
        val buyAt = current.close - openOrderOffset.toRate
        currentOrderOption = Some(Order(LongOrderType, commission, buyAt, buyAt + takeProfit.toRate, buyAt - stopLoss.toRate, current))
        currentOrderOption
      } else if (prev.macdHistogramPips > 0 && current.macdHistogramPips < 0) {
        val buyAt = current.close + openOrderOffset.toRate
        currentOrderOption = Some(Order(ShortOrderType, commission, buyAt, buyAt - takeProfit.toRate, buyAt + stopLoss.toRate, current))
        currentOrderOption
      } else None
    case (_ :+ current, Some(pendingOrder)) if pendingOrder.orderState == PendingOrder => // order execution attempt
      if ((pendingOrder.orderType == LongOrderType && pendingOrder.openAtPrice >= current.low) || (pendingOrder.orderType == ShortOrderType && pendingOrder.openAtPrice <= current.high)) {
        currentOrderOption = Some(pendingOrder.copy(orderState = ExecutedOrder, boughtAtCandle = Some(current)))
      } else if ((pendingOrder.orderType == LongOrderType && current.macdHistogramPips < 0) || (pendingOrder.orderType == ShortOrderType && current.macdHistogramPips > 0)) {
        currentOrderOption = None
      } else currentOrderOption = None
      None
    case (_ :+ current, Some(order)) if order.orderState == ExecutedOrder =>
      if ((order.orderType == LongOrderType && order.stopLoss >= current.low) ||
        (order.orderType == ShortOrderType && order.stopLoss <= current.high)) {
        orders = order.copy(orderState = StopLossOrder, closedAtPrice = order.stopLoss, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if ((order.orderType == LongOrderType && order.takeProfit <= current.high) || (order.orderType == ShortOrderType && order.takeProfit >= current.low)) {
        orders = order.copy(orderState = TakeProfitOrder, closedAtPrice = order.takeProfit, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if ((order.orderType == LongOrderType && current.macdHistogramPips < 0) || (order.orderType == ShortOrderType && current.macdHistogramPips > 0)) {
        orders = order.copy(orderState = CancelledOrder, closedAtPrice = current.close, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      }
      else None
  }

  def stats: String = {
    val profit = orders.map(_.profitPips).sum
    s"Total trades done ${orders.size}, total profit: $profit"
  }

}

