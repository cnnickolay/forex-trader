package org.nikosoft.oanda.bot.scalpers

import akka.actor.Actor
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.{MarketOrderRequest, TimeInForce}
import org.nikosoft.oanda.api.ApiModel.PositionModel.Position
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PriceValue}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{StopLossDetails, TakeProfitDetails}
import org.nikosoft.oanda.api.`def`.OrderApi.CreateOrderRequest
import org.nikosoft.oanda.instruments.Model._
import org.nikosoft.oanda.instruments.Oscillators.MACDItem

import scala.math.BigDecimal.RoundingMode._
import scalaz.Scalaz._
import scalaz.{-\/, \/-}

class ScalperActor(chart: Chart) extends Actor {

  var allCandles: Seq[CandleStick] = Seq.empty
  var currentSpread: Int = _
  val maxSpread = 15

  var position: Option[Position] = None
  val minAtr = 15
  val tradingUnits = 100

  override def preStart(): Unit = {
    updatePosition()
    position.foreach(println)
  }

  def receive = {
    case candles: Seq[CandleStick] =>
      allCandles = candles

      calculateBestParameters(candles, 15)
      println("Starting trading.........")

    case candle: CandleStick =>
      println(s"Candle received at ${candle.time}, close price ${candle.close}")
      allCandles = candle +: allCandles
      position match {
        case None if currentSpread <= maxSpread => seekForTrade()
        case Some(currentPosition) => updatePosition()
        case _ => println(s"Current spread is too big: $currentSpread")
      }

    case price: Price =>
      (price.asks.headOption.map(_.price.value) |@| price.bids.headOption.map(_.price.value)) (_ - _) match {
        case Some(spread) => currentSpread = (spread * 100000).toInt
        case None =>
      }
  }

  private def calculateBestParameters(candles: Seq[CandleStick], spread: Int) = estimate(candles, 10)

  def estimate(candles: Seq[CandleStick], range: Int) = {
    val results = candles
      .sliding(range)
      .flatMap { case (futureCandles :+ enterTrade :+ last) =>
        val enterTradeEma50Option = enterTrade.indicator[EMACandleCloseIndicator, BigDecimal]("50")
        val enterTrade100Option = enterTrade.indicator[EMACandleCloseIndicator, BigDecimal]("100")
        val enterTradeStochasticOption = enterTrade.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
        val lastStochasticOption = last.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
        val enterTradeAtrOption = enterTrade.indicator[ATRCandleIndicator, BigDecimal]("14")

        val successfulTradeMinPips = 15
        val longProfitOption = for {
          enterTradeEma50 <- enterTradeEma50Option
          enterTradeEma100 <- enterTrade100Option if enterTradeEma50 > enterTradeEma100
          enterTradeStochastic <- enterTradeStochasticOption
          lastStochastic <- lastStochasticOption if lastStochastic < 20 && enterTradeStochastic > 20
          enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
        } yield {
          val maxProfit = futureCandles.map(value => ((value.high - enterTrade.close) * 100000).toInt).max
          println(s"Long started at ${enterTrade.time} on ${enterTrade.close}, might close at $maxProfit")
          if (maxProfit >= successfulTradeMinPips) 1 else -1
        }

        val shortProfitOption = for {
          enterTradeEma50 <- enterTradeEma50Option
          enterTradeEma100 <- enterTrade100Option if enterTradeEma50 < enterTradeEma100
          enterTradeStochastic <- enterTradeStochasticOption
          lastStochastic <- lastStochasticOption if lastStochastic > 80 && enterTradeStochastic < 80
          enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
        } yield {
          val maxProfit = futureCandles.map(value => ((enterTrade.close - value.low) * 100000).toInt).max
          println(s"Short started at ${enterTrade.time} on ${enterTrade.close}, might close at $maxProfit")
          if (maxProfit >= successfulTradeMinPips) 1 else -1
        }
        (longProfitOption, shortProfitOption) match {
          case (Some(res), None) => Some(res)
          case (None, Some(res)) => Some(res)
          case _ => None
        }
      }.toList
      println(s"Percent of successful trades is ${(results.count(_ == 1).toDouble / results.size * 100).toInt}")
  }

  def seekForTrade() = {
    val candle +: last +: _ = allCandles

    val enterTradeEma50Option = candle.indicator[EMACandleCloseIndicator, BigDecimal]("50").map(_.setScale(5, HALF_DOWN))
    val enterTradeEma100Option = candle.indicator[EMACandleCloseIndicator, BigDecimal]("100").map(_.setScale(5, HALF_DOWN))
    val enterTradeStochasticOption = candle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3").map(_.setScale(5, HALF_DOWN))
    val lastStochasticOption = last.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3").map(_.setScale(5, HALF_DOWN))
    val enterTradeAtrIntOption = candle.indicator[ATRCandleIndicator, BigDecimal]("14").map(value => (value * 100000).toInt)
    val enterTradeAtrOption = candle.indicator[ATRCandleIndicator, BigDecimal]("14").map(_.setScale(5, HALF_DOWN))
    val macdOption = candle.indicator[MACDCandleCloseIndicator, MACDItem](None).flatMap(_.histogram.map(_.setScale(5, HALF_DOWN)))

    val \/-(pricing) = Api.pricingApi.pricing(AccountID(chart.accountId), Seq(InstrumentName(chart.instrument)))

    (enterTradeEma50Option |@| enterTradeEma100Option |@| enterTradeStochasticOption |@| lastStochasticOption |@| enterTradeAtrIntOption |@| macdOption) {
      (ema50, ema100, stochastic, prevStochastic, atr, macd) =>
        println(s"  Ema50 is $ema50, Ema100 is $ema100, ema50 - ema100 = ${ema50 - ema100} stochastic is $stochastic, prev stochastic is $prevStochastic, atr is $atr, macd histogram $macd")
    }

    // check if we can go long
    for {
      enterTradeEma50 <- enterTradeEma50Option
      enterTradeEma100 <- enterTradeEma100Option if enterTradeEma50 > enterTradeEma100
      enterTradeStochastic <- enterTradeStochasticOption
      lastStochastic <- lastStochasticOption if lastStochastic < 20 && enterTradeStochastic > 20
      enterTradeIntAtr <- enterTradeAtrIntOption if enterTradeIntAtr >= minAtr
      enterTradeAtr <- enterTradeAtrOption
    } yield {
      println("Creating long order")
      Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
        instrument = InstrumentName(chart.instrument),
        units = tradingUnits,
        timeInForce = TimeInForce.FOK,
        takeProfitOnFill = Option(
          TakeProfitDetails(price = PriceValue((pricing.prices.head.asks.head.price.value + enterTradeAtr / 2).rnd), timeInForce = TimeInForce.GTC)
        ),
        stopLossOnFill = Option(
          StopLossDetails(price = PriceValue((pricing.prices.head.bids.head.price.value - enterTradeAtr / 2).rnd), timeInForce = TimeInForce.GTC)
        )
      ))) match {
        case \/-(order) => updatePosition()
        case -\/(error) => println(s"Impossible to open market order")
      }
    }

    // check if we can go short
    for {
      enterTradeEma50 <- enterTradeEma50Option
      enterTradeEma100 <- enterTradeEma100Option if enterTradeEma50 < enterTradeEma100
      enterTradeStochastic <- enterTradeStochasticOption
      lastStochastic <- lastStochasticOption if lastStochastic > 80 && enterTradeStochastic < 80
      enterTradeIntAtr <- enterTradeAtrIntOption if enterTradeIntAtr >= minAtr
      enterTradeAtr <- enterTradeAtrOption
    } yield {
      println("Creating short order")
      Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
        instrument = InstrumentName(chart.instrument),
        units = -tradingUnits,
        timeInForce = TimeInForce.FOK,
        takeProfitOnFill = Option(
          TakeProfitDetails(price = PriceValue((pricing.prices.head.bids.head.price.value - enterTradeAtr / 2).rnd), timeInForce = TimeInForce.GTC)
        ),
        stopLossOnFill = Option(
          StopLossDetails(price = PriceValue((pricing.prices.head.asks.head.price.value + enterTradeAtr / 2).rnd), timeInForce = TimeInForce.GTC)
        )
      ))) match {
        case \/-(order) => updatePosition()
        case -\/(error) => println(s"Impossible to open market order")
      }
    }
  }

  def updatePosition() = position = Api.positionApi.openPositions(AccountID(chart.accountId)).toOption.flatMap(_.positions.headOption)

  implicit class BigDecimalPimp(value: BigDecimal) {
    def rnd: BigDecimal = value.setScale(5, HALF_DOWN)
  }
}
