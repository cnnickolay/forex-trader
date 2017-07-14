package org.nikosoft.oanda.instruments

import scalaz.Scalaz._

object Smoothing {

  def sma(period: Int, values: Seq[BigDecimal]): Option[BigDecimal] = (values.size < period).option(values.take(period).sum / period)

  def ema(period: Int, value: BigDecimal, lastEmaOption: Option[BigDecimal], values: Seq[BigDecimal] = Seq.empty): Option[BigDecimal] =
    (for {
      lastEma <- lastEmaOption
    } yield {
      val alpha = BigDecimal.valueOf(2) / (1 + period)
      value * alpha + lastEma * (1 - alpha)
    })
      .fold(sma(period, values))(currentEma => Option(currentEma))


}
