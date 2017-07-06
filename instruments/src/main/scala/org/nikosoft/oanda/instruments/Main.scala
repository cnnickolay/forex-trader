package org.nikosoft.oanda.instruments

import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity
import org.nikosoft.oanda.api.ApiModel.OrderModel.{MarketOrderRequest, OrderState, TimeInForce}
import org.nikosoft.oanda.api.ApiModel.PricingModel.PriceValue
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TradeModel.TradeState
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{StopLossDetails, TakeProfitDetails}
import org.nikosoft.oanda.api.`def`.OrderApi.CreateOrderRequest

import scalaz.{-\/, \/-}

object Main extends App {

  private val accountId = AccountID("001-004-1442547-003")
  private val instrument = InstrumentName("EUR_USD")

  def openOrdersAndPositions = {
    val \/-(orders) = Api.orderApi.orders(accountId = accountId, state = OrderState.PENDING)
        orders.orders.foreach(println)

    val \/-(positions) = Api.positionApi.openPositions(accountId)
        positions.positions.foreach(println)
    orders
  }

  def pricing = {
    val \/-(pricing) = Api.pricingApi.pricing(accountId, Seq(instrument))
//    pricing.prices.foreach(println)
    pricing
  }

  def trades = {
    val \/-(trades) = Api.tradeApi.trades(accountId, state = TradeState.CLOSED, instrument = instrument, count = 1)
    trades.trades.foreach(println)
    trades
  }

  def accountSummary = {
    val \/-(accounts) = Api.accounts.accountSummary(accountId)
    //    println(accounts)
    accounts
  }

  def openOrder(_long: Boolean) = {
    if (_long) println("Going Long") else println("Going Short")
    val units = 300
    val takeProfitMargin = 0.0001
    val stopLossMargin = 0.0002
    val long = CreateOrderRequest(MarketOrderRequest(
      instrument = instrument,
      units = units,
      takeProfitOnFill = Option(
        TakeProfitDetails(price = PriceValue(pricing.prices.head.asks.head.price.value + takeProfitMargin), timeInForce = TimeInForce.GTC)
      ),
      stopLossOnFill = Option(
        StopLossDetails(price = PriceValue(pricing.prices.head.bids.head.price.value - stopLossMargin), timeInForce = TimeInForce.GTC)
      )
    ))
    val short = CreateOrderRequest(MarketOrderRequest(
      instrument = instrument,
      units = -units,
      takeProfitOnFill = Option(
        TakeProfitDetails(price = PriceValue(pricing.prices.head.bids.head.price.value - takeProfitMargin), timeInForce = TimeInForce.GTC)
      ),
      stopLossOnFill = Option(
        StopLossDetails(price = PriceValue(pricing.prices.head.asks.head.price.value + stopLossMargin), timeInForce = TimeInForce.GTC)
      )
    ))
    val \/-(openedOrder) = Api.orderApi.createOrder(accountId, if (_long) long else short)
    //    println(openedOrder)
    openedOrder
  }

  var lastVolume = 0
  def streamPrices() = {
    val queue = Api.pricingApi.pricingStream(accountId = accountId, instruments = Seq(instrument), snapshot = true, terminate = false)

    Iterator.continually(queue.take()).foreach {
      case -\/(heartbeat) => //println(heartbeat)
      case \/-(price) =>
        val \/-(candle) = Api.instrumentsApi.candles(instrument = instrument, granularity = CandlestickGranularity.W, count = Some(1))
        val volume = candle.candles.head.volume - lastVolume
        lastVolume = candle.candles.head.volume
        println(s"time ${price.time}, bid ${price.bids.head.price.value}, ask ${price.asks.head.price.value}, volume $volume")
    }
  }

  var positionType = true

  def trade() = {
    while (true) {
      val orders = openOrdersAndPositions.orders
      if (orders.isEmpty) {
        val price = pricing
        val spread = ((price.prices.head.asks.head.price.value - price.prices.head.bids.head.price.value)*100000).toInt

        if (spread <= 11) {
          val \/-(lastTrade) = Api.tradeApi.trades(accountId, state = TradeState.CLOSED, instrument = instrument, count = 1)
          val lastTradePL = lastTrade.trades.head.realizedPL.value.toDouble
          if (lastTradePL < 0) positionType = !positionType
          println(s"Last trade PL $lastTradePL")

          val order = openOrder(positionType)
          println("Opened order")
          println(order)
          Thread.sleep(1000)
          println(openOrdersAndPositions.orders)
        }
      }

      Thread.sleep(3000)
    }
  }

  /*
      CandlestickGranularity.values.toList
        .map { granularity =>
          Thread.sleep(200)
          (Try(Api.instrumentsApi.candles(
            instrument = InstrumentName("EUR_USD"),
            count = None,
            from = Option(DateTime("2017-04-02T21:00:00.000Z")),
            to = Option(DateTime("2017-05-02T21:00:00.000Z")),
            granularity = granularity)),
            granularity)
        }
        .collect { case (Success(response), granularity) => (response, granularity)}
        .collect { case (\/-(candles), granularity) =>
          val pips = candles.candles.flatMap(_.mid).map(candle => candle.h.pips - candle.l.pips)
          val avg = pips.sum / pips.length
          (avg, granularity)
        }
        .sortBy { case (avg, _) => avg }
        .foreach { case (avg, granularity) => println(s"$granularity / $avg") }
  */

  /*
    val \/-(closedPositions) = Api.positionApi.closePosition(
      AccountID("001-004-1442547-003"),
      InstrumentName("EUR_USD"),
      ClosePositionRequest(shortUnits = Option("ALL"))
    )
  */

  openOrdersAndPositions
//    streamPrices()
//    trade()
  //  openOrder
  //  accountSummary
  //  trades
  //  streamPrices()
  //  println(openOrdersAndPositions)
//    trade()

/*  val \/-(candles) = Api.instrumentsApi.candles(
    instrument = instrument,
    granularity = CandlestickGranularity.M1,
    count = Some(3600)
  )

  candles.candles
    .collect { case Candlestick(time, _, _, Some(candle), volume, _) => (time, candle, volume) }
    .map { case (time, candle, volume) => (time, ((candle.h.value - candle.l.value) * 100000).toInt, ((candle.o.value - candle.c.value) * 100000).toInt, volume) }
    .foreach(println)*/

//  openOrder(true)
//  Api.positionApi.closePosition(accountId, instrument, ClosePositionRequest(longUnits = Some("ALL")))

/*
  Iterator.continually(Api.instrumentsApi.candles(
    instrument = instrument,
    granularity = CandlestickGranularity.M1,
    count = Some(2)
  )).foreach { case \/-(CandlesResponse(_, _, Seq(_, current @ Candlestick(_, _, _, Some(candle), volume, _)))) =>
    println(s"${candle.h.value - candle.l.value} / $volume")
    Thread.sleep(3000)
  }
*/

}
