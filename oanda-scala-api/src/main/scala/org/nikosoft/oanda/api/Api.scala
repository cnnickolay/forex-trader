package org.nikosoft.oanda.api

import org.nikosoft.oanda.api.`def`._
import org.nikosoft.oanda.api.impl._

object Api {

  val accounts: AccountsApi = AccountsApiImpl
  val instrumentsApi: InstrumentApi = InstrumentApiImpl
  val orderApi: OrderApi = OrderApiImpl
  val tradeApi: TradeApi = TradeApiImpl
  val transactionApi: TransactionApi = TransactionApiImpl
  val positionApi: PositionApi = PositionApiImpl
  val pricingApi: PricingApi = PricingApiImpl

}
