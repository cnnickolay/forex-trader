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

  abstract class OrderType
  case object LongOrderType extends OrderType
  case object ShortOrderType extends OrderType

  case class Trade(openCandle: CandleStick, closeCandle: CandleStick, orderType: OrderType, commission: Int) {
    def profitPips = Trade.calculateProfit(orderType, closeCandle, openCandle, commission)
    def duration = new Duration(openCandle.time, closeCandle.time).toStandardMinutes
  }

  object Trade {
    def calculateProfit(orderType: OrderType, last: CandleStick, first: CandleStick, commission: Int) = {
      val halfBuy = ((first.close - first.high).abs * pipsCoef / 2).toInt
      val halfSell = ((last.close - first.low).abs * pipsCoef / 2).toInt
      if (orderType == LongOrderType) ((last.close - first.close) * pipsCoef).toInt - commission - halfBuy
      else if (orderType == ShortOrderType) (((last.close - first.close) * -1) * pipsCoef).toInt - commission + halfSell
      else 0
    }
  }
}

class Trader(val commission: Int = 10, val takeProfit: Int = 50, val stopLoss: Int = -50) {

  import TraderModel._

  var orderTypeOption: Option[OrderType] = None
  var openCandleOption: Option[CandleStick] = None
  var trades: List[Trade] = List.empty

  def processCandles(candles: Seq[CandleStick]): Option[Trade] = (candles, openCandleOption, orderTypeOption) match {
    case (_ :+ current, Some(openCandle), Some(orderType)) =>
      val profit: Int = Trade.calculateProfit(orderType, current, openCandle, commission)
      val trendChanged = (orderType == LongOrderType && current.macdHistogramPips < 0) || (orderType == ShortOrderType && current.macdHistogramPips > 0)

      if (profit >= takeProfit || profit <= stopLoss || trendChanged) closePosition(current) else None
    case (list @ _ :+ prev :+ current, None, None) =>
      if (prev.macdHistogramPips < 0 && current.macdHistogramPips > 0) {
        openLongPosition(current)
      } else if (prev.macdHistogramPips > 0 && current.macdHistogramPips < 0) {
        openShortPosition(current)
      }
      None
  }

  def openLongPosition(candleStick: CandleStick) = {
//    println(s"Opening long position at ${candleStick.macdHistogramPips}")
    openCandleOption = Some(candleStick)
    orderTypeOption = Some(LongOrderType)
  }

  def openShortPosition(candleStick: CandleStick) = {
//    println(s"Opening short position at ${candleStick.macdHistogramPips}")
    openCandleOption = Some(candleStick)
    orderTypeOption = Some(ShortOrderType)
  }

  def closePosition(closeCandle: CandleStick): Option[Trade] =
    (openCandleOption |@| orderTypeOption) { (openCandle, orderType) =>
//      println("Closing position")
      val trade = Trade(openCandle, closeCandle, orderType, commission)
      trades = trade +: trades
      orderTypeOption = None
      openCandleOption = None
      trade
    }

  def stats: String = {
    val profit = trades.map(_.profitPips).sum
    s"Total trades done ${trades.size}, total profit: $profit"
  }

}

