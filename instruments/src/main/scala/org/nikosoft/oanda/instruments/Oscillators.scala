package org.nikosoft.oanda.instruments

object Oscillators {

  def rsi(period: Int, values: Seq[BigDecimal]): Option[BigDecimal] =
    if (values.size < period) None
    else {

      val diffs = values
        .take(period + 1)
        .reverse
        .sliding(2)
        .toList
        .map { case Seq(_this, _that) => _that - _this }

      val negative = -diffs.filter(_ < 0).sum / period
      val positive = diffs.filter(_ > 0).sum / period

      val rs = positive / negative
      val rsi: BigDecimal = 100 - (100.0 / (1.0 + rs))
      Option(rsi)
    }

  case class MACDItem(price: BigDecimal, ema12: Option[BigDecimal] = None, ema26: Option[BigDecimal] = None, macd: Option[BigDecimal] = None, signalLine: Option[BigDecimal] = None) {
    val histogram: Option[BigDecimal] = for (macdValue <- macd; signalLineValue <- signalLine) yield macdValue - signalLineValue
  }

  def macd(currentValue: BigDecimal, prevMacd: Seq[MACDItem] = Seq.empty): MACDItem = {
    val ema12 = Smoothing.ema(12, currentValue, prevMacd.headOption.flatMap(_.ema12), currentValue +: prevMacd.take(11).map(_.price))
    val ema26 = Smoothing.ema(26, currentValue, prevMacd.headOption.flatMap(_.ema26), currentValue +: prevMacd.take(25).map(_.price))
    val macd = for(ema12Value <- ema12; ema26Value <- ema26) yield ema12Value - ema26Value
    val signalLine = macd.flatMap(macdValue => Smoothing.ema(9, macdValue, prevMacd.headOption.flatMap(_.signalLine), macdValue +: prevMacd.take(8).flatMap(_.macd)))
    MACDItem(currentValue, ema12, ema26, macd, signalLine)
  }

}
