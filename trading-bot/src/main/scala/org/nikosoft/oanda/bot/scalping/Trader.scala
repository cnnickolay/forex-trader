package org.nikosoft.oanda.bot.scalping

import org.joda.time.Duration
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scalaz.Scalaz._

object TraderModel {
  val pipsCoef = 100000

  implicit class CandleStickPimped(candleStick: CandleStick) {
    def macdHistogramPips = (candleStick.indicators("MACDCandleCloseIndicator").asInstanceOf[MACDItem].histogram.getOrElse(BigDecimal(0)) * pipsCoef).toInt
  }

  abstract class OrderType
  case object LongOrderType extends OrderType
  case object ShortOrderType extends OrderType

  case class Trade(openCandle: CandleStick, closeCandle: CandleStick, orderType: OrderType, commission: Int) {
    def profitPips = Trade.calculateProfit(orderType, closeCandle, openCandle, commission)
    def duration = new Duration(openCandle.time, closeCandle.time).toStandardMinutes
  }

  object Trade {
    def calculateProfit(orderType: OrderType, current: CandleStick, openCandle: CandleStick, commission: Int) = {
      if (orderType == LongOrderType) ((current.close - openCandle.close) * pipsCoef).toInt - commission
      else if (orderType == ShortOrderType) ((openCandle.close - current.close) * pipsCoef).toInt - commission
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
    case (_ +: _ +: current +: _, Some(openCandle), Some(orderType)) =>
      val profit: Int = Trade.calculateProfit(orderType, current, openCandle, commission)
      val trendChanged = (orderType == LongOrderType && current.macdHistogramPips < 0) || (orderType == ShortOrderType && current.macdHistogramPips > 0)

      if (profit >= takeProfit || profit <= stopLoss || trendChanged) closePosition(current) else None
    case (list @ beforePrev +: prev +: current +: _, None, None) =>
      if (beforePrev.macdHistogramPips < 0 && prev.macdHistogramPips > 0 && current.macdHistogramPips > 5) {
        println(list.map(_.macdHistogramPips))
        openLongPosition(current)
      } else if (beforePrev.macdHistogramPips > 0 && prev.macdHistogramPips < 0 && current.macdHistogramPips < -5) {
        println(list.map(_.macdHistogramPips))
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

