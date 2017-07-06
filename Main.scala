package nikosoft

/** 
 * TEMPORARY FILE just to show the idea
 */

object Main extends App {

  case class Candle(close: BigDecimal)
  case class MACD(value: BigDecimal)

  trait IndicatorType[INPUT, OUTPUT] {
    def calculate: INPUT => OUTPUT
  }
  case object MACDIndicatorType extends IndicatorType[(BigDecimal, Option[MACD]), MACD] {
    def calculate: ((BigDecimal, Option[MACD])) => MACD = {
      case (value, None) => MACD(value)
      case (value, Some(prev)) => MACD(prev.value + value)
    }
  }

  abstract class Indicator[INPUT, OUTPUT] {
    val indicatorType: IndicatorType[_, OUTPUT]
    protected var values: Seq[OUTPUT] = Seq.empty
    protected def enrichFunction: INPUT => OUTPUT
    def enrich(input: INPUT): Seq[OUTPUT] = {
      val enriched = enrichFunction(input)
      values = enriched +: values
      values
    }
    def _values = values
  }

  class MACDCandleCloseIndicator extends Indicator[Candle, MACD] {
    protected def enrichFunction: Candle => MACD = candle => values match {
      case lastMACD +: tail => indicatorType.calculate((candle.close, Option(lastMACD)))
      case Nil => indicatorType.calculate((candle.close, None))
    }
    val indicatorType = MACDIndicatorType
  }

  val indicator = new MACDCandleCloseIndicator()

  indicator.enrich(Candle(10))
  indicator.enrich(Candle(10))
  indicator.enrich(Candle(10))
  indicator.enrich(Candle(10))

  indicator._values.foreach(println)

}
