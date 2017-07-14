package org.nikosoft.oanda.instruments

import scalaz.Scalaz._

object Oscillators {

  def rsi(period: Int, avgGainLoss: Option[(BigDecimal, BigDecimal)], values: Seq[BigDecimal]): Option[(BigDecimal, BigDecimal, BigDecimal)] =
    avgGainLoss.fold {
      if (values.size < period + 1) None
      else {
        val diffs = values
          .take(period + 1)
          .reverse
          .sliding(2)
          .toList
          .map { case Seq(_this, _that) => _that - _this }

        val loss = -diffs.filter(_ < 0).sum / period
        val gain = diffs.filter(_ > 0).sum / period

        val rs = gain / loss
        val rsi: BigDecimal = 100 - (100.0 / (1.0 + rs))
        (rsi, gain, loss).some
      }
    } { case (prevAvgGain, prevAvgLoss) =>
      val price +: prevPrice +: _ = values
      val gain = (price > prevPrice) ? (price - prevPrice) | 0
      val loss = (price < prevPrice) ? (prevPrice - price) | 0
      val avgGain = (prevAvgGain * (period - 1) + gain) / period
      val avgLoss = (prevAvgLoss * (period - 1) + loss) / period
      val rs = avgGain / avgLoss
      val rsi: BigDecimal = 100 - (100.0 / (1.0 + rs))
      (rsi, avgGain, avgLoss).some
    }

  case class MACDItem(price: BigDecimal, ema12: Option[BigDecimal] = None, ema26: Option[BigDecimal] = None, macd: Option[BigDecimal] = None, signalLine: Option[BigDecimal] = None) {
    val histogram: Option[BigDecimal] = (macd |@| signalLine) (_ - _)
  }

  def macd(currentValue: BigDecimal, prevMacd: Seq[MACDItem] = Seq.empty): MACDItem = {
    val ema12 = Smoothing.ema(12, currentValue, prevMacd.headOption.flatMap(_.ema12), currentValue +: prevMacd.take(11).map(_.price))
    val ema26 = Smoothing.ema(26, currentValue, prevMacd.headOption.flatMap(_.ema26), currentValue +: prevMacd.take(25).map(_.price))
    val macd = (ema12 |@| ema26) (_ - _)
    val signalLine = macd.flatMap(macdValue => Smoothing.ema(9, macdValue, prevMacd.headOption.flatMap(_.signalLine), macdValue +: prevMacd.take(8).flatMap(_.macd)))
    MACDItem(currentValue, ema12, ema26, macd, signalLine)
  }

  def cmo(period: Int, values: Seq[BigDecimal]): Option[BigDecimal] = (values.size >= period).option {
    val slice = values.take(period)
    val diffs = slice.sliding(2).map { case (head +: prev +: Nil) => head - prev }.toList
    val negative = diffs.filter(_ < 0).map(_.abs)
    val positive = diffs.filter(_ >= 0)
    val negativeSum = negative.sum
    val positiveSum = positive.sum
    val diff = positiveSum - negativeSum
    val sum = positiveSum + negativeSum
    val div = diff / sum * 100
    div
  }

  case class Stochastic(fastValue: BigDecimal, smoothed: Option[BigDecimal] = None, smoothedAgain: Option[BigDecimal] = None)
  def stochastic(period: Int,
                 smoothingPeriod: Option[Int],
                 secondSmoothingPeriod: Option[Int],
                 values: Seq[CandleStick]): Option[Stochastic] = (values.size >= period).option {
    val slice = values.take(period)
    val highest = slice.foldLeft(values.head.high)((highest, c) => (c.high > highest) ? c.high | highest)
    val lowest = slice.foldLeft(values.head.low)((lowest, c) => (c.low < lowest) ? c.low | lowest)
    val close = slice.head.close
    Stochastic((close - lowest)/(highest - lowest)*100)
  }
  
  def awesomeOscillator() = ???
}

