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

  implicit class BigDecimalPimpled(value: BigDecimal) {
    def toPips: Int = (value * pipsCoef).toInt
  }

  abstract class OrderType
  case object LongOrderType extends OrderType
  case object ShortOrderType extends OrderType

  case class Trade(openCandle: CandleStick, openPrice: BigDecimal, closeCandle: CandleStick, orderType: OrderType, commission: Int) {
    def profitPips = Trade.calculateProfit(orderType, openPrice, closeCandle, openCandle, commission)
    def duration = new Duration(openCandle.time, closeCandle.time).toStandardMinutes
  }

  object Trade {
    def calculateProfit(orderType: OrderType, openPrice: BigDecimal, last: CandleStick, first: CandleStick, commission: Int) = {
//      val halfBuy = ((first.close - first.high).abs * pipsCoef / 2).toInt
//      val halfSell = ((last.close - first.low).abs * pipsCoef / 2).toInt
      if (orderType == LongOrderType) ((last.close - openPrice) * pipsCoef).toInt - commission// - halfBuy - halfBuy/2
      else if (orderType == ShortOrderType) (((last.close - openPrice) * -1) * pipsCoef).toInt - commission// + halfSell + halfSell/2
      else 0
    }
  }
}

class Trader(val commission: Int = 10, val takeProfit: Int = 50, val stopLoss: Int = -50) {

  import TraderModel._

  var orderTypeOption: Option[OrderType] = None
  var openCandleOption: Option[CandleStick] = None
  var openPriceOption: Option[BigDecimal] = None
  var trades: List[Trade] = List.empty

  def processCandles(candles: Seq[CandleStick]): Option[Trade] = (candles, openCandleOption, orderTypeOption, openPriceOption) match {
    case (_ :+ current :+ _, Some(openCandle), Some(orderType), Some(openPrice)) =>
      val profit: Int = Trade.calculateProfit(orderType, openPrice, current, openCandle, commission)
      val trendChanged = (orderType == LongOrderType && current.macdHistogramPips < 0) || (orderType == ShortOrderType && current.macdHistogramPips > 0)

      if (profit >= takeProfit || profit <= stopLoss || trendChanged) closePosition(current) else None
    case (list @ _ :+ prev :+ current :+ future, None, None, None) =>
      val magnitude = (current.high - current.low).toPips
      val goingLong = prev.macdHistogramPips < 0 && current.macdHistogramPips > 0
      val goingShort = prev.macdHistogramPips > 0 && current.macdHistogramPips < 0
      val openPrice = if (goingLong) current.close - 15.0 / pipsCoef else current.close + 15.0 / pipsCoef
      if (goingLong) {
//        println(s"Long position at ${list.map(_.macdHistogramPips)}, $longPositionThreshold, ${current.close}, ${current.close - (threshold.toDouble / pipsCoef)}")
        openLongPosition(current, openPrice)
      } else if (goingShort) {
//        println(s"Short position at ${list.map(_.macdHistogramPips)}, $shortPositionThreshold, ${current.close}, ${current.close + (threshold.toDouble / pipsCoef)}")
        openShortPosition(current, openPrice)
      }
      None
  }

  def openLongPosition(candleStick: CandleStick, openPrice: BigDecimal) = {
//    println(s"Opening long position at ${candleStick.macdHistogramPips}")
    openPriceOption = Some(openPrice)
    openCandleOption = Some(candleStick)
    orderTypeOption = Some(LongOrderType)
  }

  def openShortPosition(candleStick: CandleStick, openPrice: BigDecimal) = {
//    println(s"Opening short position at ${candleStick.macdHistogramPips}")
    openPriceOption = Some(openPrice)
    openCandleOption = Some(candleStick)
    orderTypeOption = Some(ShortOrderType)
  }

  def closePosition(closeCandle: CandleStick): Option[Trade] =
    (openCandleOption |@| orderTypeOption |@| openPriceOption) { (openCandle, orderType, openPrice) =>
//      println("Closing position")
      val trade = Trade(openCandle, openPrice, closeCandle, orderType, commission)
      trades = trade +: trades
      orderTypeOption = None
      openCandleOption = None
      openPriceOption = None
      trade
    }

  def stats: String = {
    val profit = trades.map(_.profitPips).sum
    s"Total trades done ${trades.size}, total profit: $profit"
  }

}

