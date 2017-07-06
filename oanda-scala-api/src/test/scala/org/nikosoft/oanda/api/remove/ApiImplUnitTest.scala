package org.nikosoft.oanda.api.remove

import org.json4s.Formats
import org.json4s.jackson.JsonMethods._
import org.nikosoft.oanda.api.JsonSerializers
import org.nikosoft.oanda.api.`def`.AccountsApi.AccountsResponse
import org.scalatest.FunSuite

class ApiImplUnitTest extends FunSuite {

  implicit val formats: Formats = JsonSerializers.formats

  test("parse account json") {

    val json =
      """{"account":{"id":"001-004-1442547-003","createdTime":"2017-05-30T07:11:30.656911765Z","currency":"EUR","createdByUserID":1442547,"alias":"MT4","marginRate":"0.02","hedgingEnabled":false,"lastTransactionID":"87","balance":"77.0033","openTradeCount":1,"openPositionCount":1,"pendingOrderCount":2,"pl":"-0.9954","resettablePL":"-0.9954","financing":"-0.0013","commission":"0.0000",
        |"orders":[
        |   {"id":"85","createTime":"2017-06-15T09:08:02.732786641Z","type":"STOP_LOSS","tradeID":"82","clientTradeID":"72179822","price":"1.11000","timeInForce":"GTC","triggerCondition":"DEFAULT","state":"PENDING"},
        |   {"id":"87","createTime":"2017-06-15T09:13:56.368495444Z","replacesOrderID":"84","type":"TAKE_PROFIT","tradeID":"82","clientTradeID":"72179822","price":"1.11850","timeInForce":"GTC","triggerCondition":"DEFAULT","state":"PENDING"}
        |],
        |"positions":[
        |  {"instrument":"EUR_USD",
        |  "long":{"units":"5000","averagePrice":"1.11810","pl":"0.3444","resettablePL":"0.3444","financing":"-0.0024","tradeIDs":["82"],"unrealizedPL":"-2.4386"},
        |  "short":{"units":"0","pl":"-1.3398","resettablePL":"-1.3398","financing":"0.0011","unrealizedPL":"0.0000"},
        |  "pl":"-0.9954","resettablePL":"-0.9954","financing":"-0.0013","commission":"0.0000","unrealizedPL":"-2.4386"}],
        |"trades":[{"id":"82","instrument":"EUR_USD","price":"1.11810","openTime":"2017-06-15T09:07:28.287005040Z","initialUnits":"1000","state":"OPEN","currentUnits":"1000","realizedPL":"0.0000","financing":"0.0000",
        |  "clientExtensions":{"id":"72179822","tag":"0"},"takeProfitOrderID":"87","stopLossOrderID":"85","unrealizedPL":"-2.4386"}],
        |"unrealizedPL":"-2.4386","NAV":"74.5647","marginUsed":"20.0022","marginAvailable":"54.5625","positionValue":"1000.1076","marginCloseoutUnrealizedPL":"-2.3847","marginCloseoutNAV":"74.6186","marginCloseoutMarginUsed":"20.0000","marginCloseoutPositionValue":"1000.0000","marginCloseoutPercent":"0.13401","withdrawalLimit":"54.5625","marginCallMarginUsed":"20.0000","marginCallPercent":"0.26803"},
        |"lastTransactionID":"87"}""".stripMargin

    println(parse(json).children.head.extract[AccountsResponse])
  }

}
