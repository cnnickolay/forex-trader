package org.nikosoft.oanda.instruments

import scala.math.BigDecimal.RoundingMode

object Converters {

  trait BigDecimalRounder {
    def round(value: BigDecimal): BigDecimal
  }

  class DefaultBigDecimalRounder(val digitsAfterPoint: Int) extends BigDecimalRounder {
    def round(value: BigDecimal): BigDecimal = value.setScale(digitsAfterPoint, RoundingMode.HALF_DOWN)
  }

  object DefaultBigDecimalRounder {
    def apply(digitsAfterPoint: Int = 6): DefaultBigDecimalRounder = new DefaultBigDecimalRounder(digitsAfterPoint)
  }

  implicit class BigDecimalPimped(value: BigDecimal) {
    def r(scale: Int): BigDecimal = DefaultBigDecimalRounder(scale).round(value)
  }

}
