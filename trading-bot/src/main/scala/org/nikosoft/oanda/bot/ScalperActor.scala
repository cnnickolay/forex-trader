package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.{MarketOrderRequest, TimeInForce}
import org.nikosoft.oanda.api.ApiModel.PositionModel.Position
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PriceValue}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{StopLossDetails, TakeProfitDetails}
import org.nikosoft.oanda.api.`def`.OrderApi.CreateOrderRequest
import org.nikosoft.oanda.api.`def`.PositionApi.ClosePositionRequest
import org.nikosoft.oanda.instruments.Model._

import scalaz.Scalaz._
import scalaz.{-\/, \/-}

class ScalperActor(chart: Chart) extends Actor {

  var allCandles: Seq[CandleStick] = Seq.empty
  var currentSpread: Int = _
  var position: Option[Position] = None

  val takeProfitMargin = 0.0001
  val stopLossMargin = 0.00005

  def receive = {
    case candles: Seq[CandleStick] =>
      allCandles = candles

      val profitLoss = calculateBestParameters(candles, 15)

      println("--------------- Profitable trades")
      profitLoss.head._2._2.foreach { case (enter, exit) =>
        println(s"Bought for ${enter.close} at ${enter.time} sold for ${exit.close} at ${exit.time}, profit ${((enter.close - exit.close).abs * 100000).toInt} pips")
      }
      println("--------------- Loss trades")
      profitLoss.head._3._2.foreach { case (enter, exit) =>
        println(s"Bought for ${enter.close} at ${enter.time} sold for ${exit.close} at ${exit.time}, loss ${((enter.close - exit.close).abs * 100000).toInt} pips")
      }

      println("--------------- Summary")
      profitLoss.take(20).foreach { case (gain, profit, loss, range, minAtr) =>
        println(s"$range candles passed with min ATR $minAtr after order, profit ${profit._1}, loss ${loss._1}, gain $gain")
      }
      println("Starting trading.........")

    case candle: CandleStick =>
      position match {
        case None => seekForTrade(candle)
        case Some(currentPosition) => updatePosition()
      }

    case price: Price =>
      (price.asks.headOption.map(_.price.value) |@| price.bids.headOption.map(_.price.value)) (_ - _) match {
        case Some(spread) => currentSpread = (spread * 100000).toInt
        case None =>
      }
  }

  private def calculateBestParameters(candles: Seq[CandleStick], spread: Int): Seq[(Int, (Int, Seq[(CandleStick, CandleStick)]), (Int, Seq[(CandleStick, CandleStick)]), Int, Int)] = {
    val profitLoss = for {
      range <- 3 to 10
      minAtr <- 3 to 20
    } yield {
      val (profit, loss) = estimate(candles, range, minAtr, spread)
      (profit._1 + loss._1, profit, loss, range, minAtr)
    }
    profitLoss.sortBy(_._1).reverse
  }

  def estimate(candles: Seq[CandleStick], range: Int, minAtr: Int, spread: Int): ((Int, Seq[(CandleStick, CandleStick)]), (Int, Seq[(CandleStick, CandleStick)])) = {
    val trades: List[(Int, CandleStick, CandleStick)] = candles.sliding(range)
      .flatMap { case (exitTrade +: _ :+ enterTrade :+ last) =>
        val enterTradeEma50Option = enterTrade.indicator[EMACandleCloseIndicator, BigDecimal]("50")
        val enterTrade100Option = enterTrade.indicator[EMACandleCloseIndicator, BigDecimal]("100")
        val enterTradeStochasticOption = enterTrade.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
        val lastStochasticOption = last.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
        val enterTradeAtrOption = enterTrade.indicator[ATRCandleIndicator, BigDecimal]("14")

        val longTrade = for {
          enterTradeEma50 <- enterTradeEma50Option
          enterTradeEma100 <- enterTrade100Option if enterTradeEma50 > enterTradeEma100
          enterTradeStochastic <- enterTradeStochasticOption
          lastStochastic <- lastStochasticOption if lastStochastic < 20 && enterTradeStochastic > 20
          enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
        } yield ((exitTrade.close - enterTrade.close) * 100000).toInt - spread

        val shortTrade = for {
          enterTradeEma50 <- enterTradeEma50Option
          enterTradeEma100 <- enterTrade100Option if enterTradeEma50 < enterTradeEma100
          enterTradeStochastic <- enterTradeStochasticOption
          lastStochastic <- lastStochasticOption if lastStochastic > 80 && enterTradeStochastic < 80
          enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
        } yield (((exitTrade.close - enterTrade.close) * 100000).toInt * -1) - spread

        (longTrade, shortTrade) match {
          case (Some(long), None) => Some(long, enterTrade, exitTrade)
          case (None, Some(short)) => Some(short, enterTrade, exitTrade)
          case _ => None
        }
      }.toList

    def sumUpTrades(allTrades: List[(Int, CandleStick, CandleStick)]): (Int, Seq[(CandleStick, CandleStick)]) =
      allTrades.foldLeft((0, Seq.empty[(CandleStick, CandleStick)])) {
        case ((totalProfit, totalTrades), (_profit, enter, exit)) => (totalProfit + _profit, (enter, exit) +: totalTrades)
      }

    val profit = sumUpTrades(trades.filter(_._1 > 0))
    val loss = sumUpTrades(trades.filter(_._1 <= 0))
    (profit, loss)
  }

  def seekForTrade(candle: CandleStick) = {
    val last = allCandles.head
    allCandles = candle +: allCandles
//    val (profit, _, _, range, minAtr) = calculateBestParameters(allCandles, currentSpread).head
//    println(s"New candle arrived at ${candle.time} with close price ${candle.close}, best range $range, best profit $profit, best atr $minAtr for spread $currentSpread")

    val enterTradeEma50Option = candle.indicator[EMACandleCloseIndicator, BigDecimal]("50")
    val enterTrade100Option = candle.indicator[EMACandleCloseIndicator, BigDecimal]("100")
    val enterTradeStochasticOption = candle.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
    val lastStochasticOption = last.indicator[StochasticCandleIndicator, BigDecimal]("5_3_3")
    val enterTradeAtrOption = candle.indicator[ATRCandleIndicator, BigDecimal]("14")

    val \/-(pricing) = Api.pricingApi.pricing(AccountID(chart.accountId), Seq(InstrumentName(chart.instrument)))

    // check if we can go long
    for {
      enterTradeEma50 <- enterTradeEma50Option
      enterTradeEma100 <- enterTrade100Option if enterTradeEma50 > enterTradeEma100
      enterTradeStochastic <- enterTradeStochasticOption
      lastStochastic <- lastStochasticOption if lastStochastic < 20 && enterTradeStochastic > 20
    //        enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
    } yield {
      Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
        instrument = InstrumentName(chart.instrument),
        units = 50,
        timeInForce = TimeInForce.FOK,
        takeProfitOnFill = Option(
          TakeProfitDetails(price = PriceValue(pricing.prices.head.asks.head.price.value + takeProfitMargin), timeInForce = TimeInForce.GTC)
        ),
        stopLossOnFill = Option(
          StopLossDetails(price = PriceValue(pricing.prices.head.bids.head.price.value - stopLossMargin), timeInForce = TimeInForce.GTC)
        )
      ))) match {
        case \/-(order) => updatePosition()
        case -\/(error) => println(s"Impossible to open market order")
      }
    }

    Api.positionApi.closePosition(
      AccountID(chart.accountId),
      InstrumentName(chart.instrument),
      ClosePositionRequest(shortUnits = Option("ALL"), longUnits = Option("ALL")))

    // check if we can go short
    for {
      enterTradeEma50 <- enterTradeEma50Option
      enterTradeEma100 <- enterTrade100Option if enterTradeEma50 < enterTradeEma100
      enterTradeStochastic <- enterTradeStochasticOption
      lastStochastic <- lastStochasticOption if lastStochastic > 80 && enterTradeStochastic < 80
//      enterTradeAtr <- enterTradeAtrOption.map(value => (value * 100000).toInt) if enterTradeAtr >= minAtr
    } yield {
      Api.orderApi.createOrder(AccountID(chart.accountId), CreateOrderRequest(MarketOrderRequest(
        instrument = InstrumentName(chart.instrument),
        units = -50,
        timeInForce = TimeInForce.FOK,
        takeProfitOnFill = Option(
          TakeProfitDetails(price = PriceValue(pricing.prices.head.bids.head.price.value - takeProfitMargin), timeInForce = TimeInForce.GTC)
        ),
        stopLossOnFill = Option(
          StopLossDetails(price = PriceValue(pricing.prices.head.asks.head.price.value + stopLossMargin), timeInForce = TimeInForce.GTC)
        )
      ))) match {
        case \/-(order) => updatePosition()
        case -\/(error) => println(s"Impossible to open market order")
      }
    }
  }

  def updatePosition() = position = Api.positionApi.openPositions(AccountID(chart.accountId)).toOption.map(_.positions.head)
}
