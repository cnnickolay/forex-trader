package org.nikosoft.oanda.bot.scalping

import org.joda.time.Duration
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.math.BigDecimal.RoundingMode
import scalaz.Scalaz._


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

  abstract class OrderType
  case object LongOrderType extends OrderType
  case object ShortOrderType extends OrderType

  abstract class OrderState
  case object PendingOrder extends OrderState
  case object ExecutedOrder extends OrderState
  case object TakeProfitOrder extends OrderState
  case object StopLossOrder extends OrderState
  case object ClosedByTimeOrder extends OrderState
  case object CancelledOrder extends OrderState

  case class Order(orderType: OrderType, openAtPrice: Option[BigDecimal] = None, commissionPips: Int,
                   takeProfit: Int, stopLoss: Int,
                   createdAtCandle: CandleStick, closedAtPrice: BigDecimal = 0, boughtAtCandle: Option[CandleStick] = None,
                   closedAtCandle: Option[CandleStick] = None, orderState: OrderState = PendingOrder,
                   takeProfitAtPrice: Option[BigDecimal] = None, stopLossAtPrice: Option[BigDecimal] = None) {
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
