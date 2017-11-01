package org.nikosoft.oanda.bot.scalping

import org.joda.time.Duration
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.math.BigDecimal.RoundingMode
import scalaz.Scalaz._
import scalaz._

object TraderModel {
  val pipsCoef = 100000

  implicit class CandleStickPimped(candleStick: CandleStick) {
    def macdHistogram = candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0))
    def macdHistogramPips = (candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0)) * pipsCoef).toInt
    def sma(precision: String) = candleStick.indicators.get(s"SMACandleCloseIndicator_$precision").map(_.asInstanceOf[BigDecimal].setScale(5, RoundingMode.HALF_UP).toDouble).getOrElse(0.0)
    def ema20 = candleStick.indicators.get("EMACandleCloseIndicator_20").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema30 = candleStick.indicators.get("EMACandleCloseIndicator_30").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema40 = candleStick.indicators.get("EMACandleCloseIndicator_40").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema50 = candleStick.indicators.get("EMACandleCloseIndicator_50").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
    def ema100 = candleStick.indicators.get("EMACandleCloseIndicator_100").map(_.asInstanceOf[BigDecimal].toDouble).getOrElse(0.0)
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

  case class Order(orderType: OrderType, commissionPips: Int, openAtPrice: Option[BigDecimal] = None,
                   takeProfit: Int, stopLoss: Int, createdAtCandle: CandleStick,
                   closedAtPrice: BigDecimal = 0, boughtAtCandle: Option[CandleStick] = None,
                   closedAtCandle: Option[CandleStick] = None, orderState: OrderState = PendingOrder) {
    def profitPips = ((orderType, openAtPrice) match {
      case (LongOrderType, Some(openedAtPrice)) => closedAtPrice - openedAtPrice
      case (ShortOrderType, Some(openedAtPrice)) => openedAtPrice - closedAtPrice
    }).toPips - commissionPips

    def duration = (boughtAtCandle |@| closedAtCandle) { (b, c) =>
      new Duration(b.time, c.time).toStandardHours.toString
    }.getOrElse("")
  }

  def calculateProfit(orderType: OrderType, openPrice: BigDecimal, last: CandleStick, first: CandleStick, commission: Int) = {
    if (orderType == LongOrderType) ((last.high - openPrice) * pipsCoef).toInt - commission
    else if (orderType == ShortOrderType) ((openPrice - last.low) * pipsCoef).toInt - commission
    else 0
  }
}

class Trader(val commission: Int = 10, val openOrderOffset: Int = 45, val takeProfit: Int = 200, val stopLoss: Int = 50, val minTakeProfit: Int = 200) {

  import TraderModel._

  var currentOrderOption: Option[Order] = None
  var orders: List[Order] = List.empty

  def processCandles(candles: Seq[CandleStick]): Option[Order] = (candles, currentOrderOption) match {
    case (_ :+ pprev :+ prev :+ current, None) => // order creation
//      if (current.ema20 > current.ema30 && prev.ema20 < prev.ema30 && current.close < current.ema30) {
//        currentOrderOption = Some(Order(LongOrderType, commission, Some(current.close - openOrderOffset.toRate), takeProfit, stopLoss, current))
//      }
      val takeProfit = (current.close - current.sma("168")).toPips
      if (takeProfit.abs >= minTakeProfit) currentOrderOption = Some(Order(if (takeProfit < 0) LongOrderType else ShortOrderType, commission, None, takeProfit.abs, stopLoss, current))
      currentOrderOption
    case (_ :+ previous :+ current, Some(pendingOrder)) if pendingOrder.orderState == PendingOrder => // order execution attempt
      if (pendingOrder.openAtPrice.isEmpty) {
        currentOrderOption = Some(pendingOrder.copy(orderState = ExecutedOrder, boughtAtCandle = Some(current), openAtPrice = Some(current.open)))
      } else if ((pendingOrder.orderType == LongOrderType && ~pendingOrder.openAtPrice >= current.low) || (pendingOrder.orderType == ShortOrderType && ~pendingOrder.openAtPrice <= current.high)) {
        currentOrderOption = Some(pendingOrder.copy(orderState = ExecutedOrder, boughtAtCandle = Some(current)))
      } else currentOrderOption = None
      currentOrderOption
    case (_ :+ current, Some(order)) if order.orderState == ExecutedOrder =>
      val takeProfitRate = ~order.openAtPrice + order.takeProfit.toRate * (if (order.orderType == ShortOrderType) -1 else 1)
      val stopLossRate = ~order.openAtPrice + order.stopLoss.toRate * (if (order.orderType == ShortOrderType) 1 else -1)

      val (lowest, highest) = if (current.close > current.open) (current.open, current.close) else (current.close, current.open)

      if ((order.orderType == LongOrderType && stopLossRate >= lowest) ||
        (order.orderType == ShortOrderType && stopLossRate <= highest)) {
        orders = order.copy(orderState = StopLossOrder, closedAtPrice = stopLossRate, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if ((order.orderType == LongOrderType && takeProfitRate <= current.high) || (order.orderType == ShortOrderType && takeProfitRate >= current.low)) {
        orders = order.copy(orderState = TakeProfitOrder, closedAtPrice = takeProfitRate, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if (order.boughtAtCandle.exists(boughtAt => new Duration(boughtAt.time, current.time).toStandardDays.getDays >= 1)) {
        orders = order.copy(orderState = StopLossOrder, closedAtPrice = current.close, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else None
  }

/*
  def processCandles(candles: Seq[CandleStick]): Option[Order] = (candles, currentOrderOption) match {
    case (_ :+ pprev :+ prev :+ current, None) => // order creation
      if (prev.macdHistogram < 0 && current.macdHistogram > 0) {
        val buyAt = prev.close + (pprev.close - prev.close) - openOrderOffset.toRate
        currentOrderOption = Some(Order(LongOrderType, commission, None, takeProfit, stopLoss, current))
        currentOrderOption
      } else if (prev.macdHistogram > 0 && current.macdHistogram < 0) {
        val buyAt = prev.close + (pprev.close - prev.close) + openOrderOffset.toRate
        currentOrderOption = Some(Order(ShortOrderType, commission, None, takeProfit, stopLoss, current))
        currentOrderOption
      } else None
    case (_ :+ previous :+ current, Some(pendingOrder)) if pendingOrder.orderState == PendingOrder => // order execution attempt
      if (pendingOrder.openAtPrice.isEmpty) {
        currentOrderOption = Some(pendingOrder.copy(orderState = ExecutedOrder, boughtAtCandle = Some(current), openAtPrice = Some(current.open)))
      } else if ((pendingOrder.orderType == LongOrderType && ~pendingOrder.openAtPrice >= current.low) || (pendingOrder.orderType == ShortOrderType && ~pendingOrder.openAtPrice <= current.high)) {
        currentOrderOption = Some(pendingOrder.copy(orderState = ExecutedOrder, boughtAtCandle = Some(current)))
      } else if ((pendingOrder.orderType == LongOrderType && previous.macdHistogram > current.macdHistogram) || (pendingOrder.orderType == ShortOrderType && previous.macdHistogram < current.macdHistogram)) {
        currentOrderOption = None
      }// else currentOrderOption = None
      None
    case (_ :+ current, Some(order)) if order.orderState == ExecutedOrder =>
      val takeProfitRate = ~order.openAtPrice + order.takeProfit.toRate * (if (order.orderType == ShortOrderType) -1 else 1)
      val stopLossRate = ~order.openAtPrice + order.stopLoss.toRate * (if (order.orderType == ShortOrderType) 1 else -1)

      val (lowest, highest) = if (current.close > current.open) (current.open, current.close) else (current.close, current.open)

      if ((order.orderType == LongOrderType && stopLossRate >= lowest) ||
        (order.orderType == ShortOrderType && stopLossRate <= highest)) {
        orders = order.copy(orderState = StopLossOrder, closedAtPrice = (order.orderType == LongOrderType) ? lowest | highest, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if ((order.orderType == LongOrderType && takeProfitRate <= current.high) || (order.orderType == ShortOrderType && takeProfitRate >= current.low)) {
        orders = order.copy(orderState = TakeProfitOrder, closedAtPrice = takeProfitRate, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      } else if ((order.orderType == LongOrderType && current.macdHistogramPips < 0) || (order.orderType == ShortOrderType && current.macdHistogramPips > 0)) {
        orders = order.copy(orderState = CancelledOrder, closedAtPrice = current.close, closedAtCandle = Some(current)) +: orders
        currentOrderOption = None
        orders.headOption
      }
      else None
  }
*/

  def stats: String = {
    val profitList = orders.map(_.profitPips)
    val positives = profitList.count(_ > 0)
    val negatives = profitList.count(_ < 0)
    val profit = profitList.sum
    s"Total trades done ${orders.size}, total profit: $profit, positives: $positives, negatives: $negatives"
  }

}

