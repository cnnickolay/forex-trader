package org.nikosoft.oanda.instruments

import org.nikosoft.oanda.instruments.Model.CandleStick

object Indicators {

  def atr(period: Int, candles: Seq[CandleStick], previousValueOption: Option[BigDecimal]): Option[BigDecimal] = previousValueOption.fold {
    if (candles.size < period) None
    else {
      val reversed = candles.reverse
      val high = reversed.map(_.high)
      val low = reversed.map(_.low)
      val close = reversed.map(_.close)
      val highLowDiff = (high, low).zipped.map(_ - _)
      val highCloseDiff = (BigDecimal.valueOf(0) +: (high.tail, close).zipped.map(_ - _)).map(_.abs)
      val lowCloseDiff = (BigDecimal.valueOf(0) +: (high.tail, close).zipped.map(_ - _)).map(_.abs)
      val values = (highLowDiff, highCloseDiff, lowCloseDiff).zipped.map(Seq(_, _, _)).map(_.max)
      Option(values.sum / values.size)
    }
  } {
    previousValue => candles match {
      case current +: previous +: _ =>
        val highLowDiff = current.high - current.low
        val highCloseDiff = current.high - previous.close
        val lowCloseDiff = current.low - previous.close
        val max = Seq(highLowDiff, highCloseDiff, lowCloseDiff).max
        Option((previousValue * 13 + max) / 14)
      case _ => None
    }
  }

}
