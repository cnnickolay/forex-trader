package org.nikosoft.oanda

import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickGranularity}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{TransactionFilter, TransactionID}
import org.nikosoft.oanda.api.`def`.InstrumentApi.CandlesResponse
import org.nikosoft.oanda.api.`def`.PositionApi.ClosePositionRequest
import org.nikosoft.oanda.api.impl.{AccountsApiImpl, InstrumentApiImpl, TransactionApiImpl}

import scala.concurrent.duration.DurationInt
import scalaz.{-\/, \/-}

/**
  * Sandbox for experiments
  */
object Sandbox extends App {

/*
  AccountsApiImpl.accounts match {
    case \/-(result) =>
      result.accounts.foreach(println)
      result.accounts.map(account => AccountsApiImpl.accountDetails(account.id)).foreach(println)
      result.accounts.map(account => AccountsApiImpl.accountSummary(account.id)).foreach(println)
      result.accounts.map(account => AccountsApiImpl.accountInstruments(account.id, Seq(InstrumentName("EUR_USD"), InstrumentName("EUR_CHF")))).foreach(println)
    case -\/(error) => println(error)
  }
*/

/*
  InstrumentApiImpl.candles(InstrumentName("EUR_USD"), granularity = CandlestickGranularity.H1, count = Option(10)) match {
    case \/-(response) => response.candles.foreach(println)
    case -\/(err) =>
  }
*/

  val accountId = AccountID(System.getProperty("accountID"))

//  OrderApiImpl.order(AccountID(accountId), OrderRequestWrapper(LimitOrderRequest(instrument = InstrumentName("EUR_USD"), units = 1000, price = PriceValue("0.1"))))

//  val \/-(orders) = OrderApiImpl.orders(AccountID(accountId), state = OrderState.FILLED)
//  orders.orders.foreach(println)

//  println(OrderApiImpl.cancelOrder(AccountID(accountId), OrderSpecifier("237")))

//  val \/-(transactions) = TransactionApiImpl.transactionsIdRange(AccountID(accountId), from = TransactionID("1"), to = TransactionID("200"), `type` = Seq(TransactionFilter.ADMIN, TransactionFilter.LIMIT_ORDER))
//  transactions.transactions.foreach(println)

  import scala.concurrent.ExecutionContext.Implicits.global

/*
  var x = 0
  val queue = TransactionApiImpl.transactionsStream(AccountID(accountId), terminate = {x = x + 1; x >= 3})

  Iterator.continually(queue.take()).foreach {
    case \/-(-\/(heartbeat)) => println(heartbeat)
    case \/-(\/-(transaction)) => println(transaction)
    case -\/(error) => println(error)
  }
*/

/*
  val \/-(candles: CandlesResponse) = Api.instrumentsApi.candles(InstrumentName("EUR_USD"), granularity = CandlestickGranularity.M5, count = Option(100))
  candles.candles.flatMap(_.mid).map(candle => candle.h.pips - candle.l.pips).foreach(println)
*/


/*
  Api.instrumentsApi
    .candles(
      instrument = InstrumentName("EUR_USD"),
      granularity = CandlestickGranularity.M1,
//      count = Some(500)
      from = Some(DateTime("2017-07-01T00:00:00Z")),
      to = Some(DateTime("2017-07-05T00:00:00Z"))
    ).fold(println, response => {
    response.candles.take(10).foreach(println)
  })
*/

  Api.positionApi.closePosition(AccountID(GlobalProperties.TradingAccountId), InstrumentName("EUR_USD"), ClosePositionRequest(longUnits = Some("ALL")))
}
