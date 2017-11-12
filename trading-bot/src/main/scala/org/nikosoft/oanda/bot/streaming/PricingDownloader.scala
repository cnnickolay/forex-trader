package org.nikosoft.oanda.bot.streaming

import org.nikosoft.oanda.GlobalProperties
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.`def`.PricingApi

import scalaz.\/-

object PricingDownloader extends App {

  val accountId = AccountID(GlobalProperties.TradingAccountId)
  val eurUsd = InstrumentName("EUR_USD")

  val \/-(pricingResponse: PricingApi.PricingResponse) = Api.pricingApi.pricing(accountId, Seq(eurUsd), Option(DateTime("2017-11-04T23:46:41.857Z")))
  println(pricingResponse)

}
