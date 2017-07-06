package org.nikosoft.oanda.bot

import akka.actor.Actor
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.{MarketOrderRequest, Order, OrderState, TimeInForce}
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PriceValue}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{StopLossDetails, TakeProfitDetails}
import org.nikosoft.oanda.api.`def`.OrderApi.CreateOrderRequest
import org.nikosoft.oanda.api.`def`.PositionApi.ClosePositionRequest
import org.nikosoft.oanda.instruments.Converters._
import org.nikosoft.oanda.instruments.{Converters, Oscillators, Smoothing}

import scalaz.Scalaz._
import scalaz.\/-

object TraderActor {

  case object Tick

}

class TraderActor(accountId: String, instrument: String) extends Actor {

  var spread: Int = _
  var lastAsk: BigDecimal = _
  var lastBid: BigDecimal = _
  var averagePrice: BigDecimal = _
  var rsi: BigDecimal = 0
  var lastEma: BigDecimal = 0
  var emaChange: BigDecimal = 0
  val emaRange = 60
  var allEmaChanges: Seq[BigDecimal] = Seq.empty
  var allPrices: Seq[BigDecimal] = Seq.empty
  val trendMinSize = 20
  val spreadThreshold = 12

  implicit val rounder = DefaultBigDecimalRounder(10)

  def receive: Receive = {
    case price: Price =>
      //      println(price)
      lastAsk = price.asks.head.price.value
      lastBid = price.bids.head.price.value
      spread = ((lastAsk - lastBid) * 100000).toInt
      averagePrice = (lastAsk + lastBid) / 2
      allPrices = averagePrice +: allPrices
      lastEma = (lastEma == 0) ? averagePrice | lastEma
      val Some(ema) = Smoothing.ema(emaRange, averagePrice, Option(lastEma))
      emaChange = (ema - lastEma) * 100000
      lastEma = ema
      allEmaChanges = emaChange +: allEmaChanges
      rsi = Oscillators.rsi(100, allPrices).get
      println(s"last price $averagePrice, ema change $emaChange, rsi $rsi")
//      trade()
    case unknown => println(s"Unexpected message $unknown")
  }

  def trade() = {
    val orders: Seq[Order] = openOrdersAndPositions.orders

    if (allEmaChanges.size >= trendMinSize && orders.isEmpty && spread <= spreadThreshold) {
      val positive = allEmaChanges.take(trendMinSize).forall(_ > 0)
      val negative = allEmaChanges.take(trendMinSize).forall(_ < 0)

      (positive, negative) match {
        case (true, _) => openOrder(true)
        case (_, true) => openOrder(false)
        case _ => None
      }
    }
  }

  def openOrder(long: Boolean) = {
    if (long) println("Opening Long Order") else println("Opening Short Order")
    val units = 500
    val takeProfitMargin = 0.0001
    val stopLossMargin = 0.0005
    val longOrder = CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(instrument),
      units = units,
      takeProfitOnFill = Option(
        TakeProfitDetails(price = PriceValue(lastAsk + takeProfitMargin), timeInForce = TimeInForce.GTC)
      ),
      stopLossOnFill = Option(
        StopLossDetails(price = PriceValue(lastBid - stopLossMargin), timeInForce = TimeInForce.GTC)
      )
    ))
    val shortOrder = CreateOrderRequest(MarketOrderRequest(
      instrument = InstrumentName(instrument),
      units = -units,
      takeProfitOnFill = Option(
        TakeProfitDetails(price = PriceValue(lastBid - takeProfitMargin), timeInForce = TimeInForce.GTC)
      ),
      stopLossOnFill = Option(
        StopLossDetails(price = PriceValue(lastAsk + stopLossMargin), timeInForce = TimeInForce.GTC)
      )
    ))
    val \/-(openedOrder) = Api.orderApi.createOrder(AccountID(accountId), if (long) longOrder else shortOrder)
    println(openedOrder)
    openedOrder
  }

  def openOrdersAndPositions = {
    val \/-(orders) = Api.orderApi.orders(accountId = AccountID(accountId), state = OrderState.PENDING)
    //    orders.orders.foreach(println)

    val \/-(positions) = Api.positionApi.openPositions(AccountID(accountId))
    //    positions.positions.foreach(println)
    orders
  }

  def closePositions() = {
    Api.positionApi.closePosition(AccountID(accountId), InstrumentName(instrument), ClosePositionRequest(longUnits = Some("ALL"), shortUnits = Some("ALL")))
  }

}
