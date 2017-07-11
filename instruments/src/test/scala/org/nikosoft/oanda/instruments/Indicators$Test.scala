package org.nikosoft.oanda.instruments

import org.joda.time.Instant
import org.nikosoft.oanda.instruments.Model.CandleStick
import org.scalatest.{FunSpec, Matchers}

class Indicators$Test extends FunSpec with Matchers {

  describe("atr") {

    it("should calculate Average True Range when there is no previous value provided") {
      val inputHigh: Seq[BigDecimal] = Seq(50.19, 49.88, 49.66, 50.12, 50.19, 49.92, 49.35, 49.20, 49.05, 48.82, 48.87, 48.90, 48.72, 48.70)
      val inputLow: Seq[BigDecimal] = Seq(49.73, 49.43, 48.90, 49.20, 49.87, 49.50, 48.86, 48.94, 48.64, 48.24, 48.37, 48.39, 48.14, 47.79)
      val inputClose: Seq[BigDecimal] = Seq(50.03, 49.75, 49.50, 49.53, 50.13, 49.91, 49.32, 49.07, 49.03, 48.74, 48.63, 48.75, 48.61, 48.16)
      val input = (inputHigh, inputLow, inputClose).zipped.map(CandleStick(Instant.now, 0, _, _, _, 0, complete = true))
      val actual = Indicators.atr(14, input, None)
      val expected: BigDecimal = 0.56
      actual.fold(fail("should not fail here")) { actualValue =>
        actualValue shouldBe (expected +- 0.01)
      }
    }

    it("should calculate Average True Range when previous value provided") {
      val inputCandle = Seq(
        CandleStick(Instant.now(), 0, 50.36, 49.26, 50.31, 0, complete = true),
        CandleStick(Instant.now(), 0, 50.19, 49.73, 50.03, 0, complete = true))
      val previousAtr: BigDecimal = 0.56
      val actual = Indicators.atr(14, inputCandle, Some(previousAtr))
      val expected: BigDecimal = 0.59
      actual.fold(fail("should not fail here")) { actualValue =>
        actualValue shouldBe (expected +- 0.01)
      }
    }

    it("should return None if no previous value provided and amount of candles is less than period") {
      val inputCandle = Seq(
        CandleStick(Instant.now(), 0, 50.36, 49.26, 50.31, 0, complete = true),
        CandleStick(Instant.now(), 0, 50.19, 49.73, 50.03, 0, complete = true),
        CandleStick(Instant.now(), 0, 50.19, 49.73, 50.03, 0, complete = true),
        CandleStick(Instant.now(), 0, 50.19, 49.73, 50.03, 0, complete = true)
      )
      val actual = Indicators.atr(6, inputCandle, None)
      actual shouldBe None
    }

    it("should return None if previous value specified but amount of candles is less than 2") {
      val inputCandle = Seq(CandleStick(Instant.now(), 0, 50.36, 49.26, 50.31, 0, complete = true))
      val actual = Indicators.atr(14, inputCandle, Option(1))
      actual shouldBe None
    }

  }

}
