package org.nikosoft.oanda.instruments

object Smoothing {

  def sma(period: Int, values: Seq[BigDecimal]): Option[BigDecimal] =
    if (values.size < period) None
    else Some(values.take(period).sum / period)

  def ema(period: Int, value: BigDecimal, lastEmaOption: Option[BigDecimal], values: Seq[BigDecimal] = Seq.empty): Option[BigDecimal] =
    (for {
      lastEma <- lastEmaOption
    } yield {
      val alpha = BigDecimal.valueOf(2) / (1 + period)
      value * alpha + lastEma * (1 - alpha)
    })
      .fold(sma(period, values))(currentEma => Option(currentEma))


}
