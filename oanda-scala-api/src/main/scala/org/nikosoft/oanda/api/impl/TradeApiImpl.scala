package org.nikosoft.oanda.api.impl

import org.apache.http.client.fluent.Request
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TradeModel.TradeID
import org.nikosoft.oanda.api.ApiModel.TradeModel.TradeState._
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.TradeApi.TradesResponse
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.`def`.TradeApi

import scalaz.\/

private[api] object TradeApiImpl extends TradeApi with ApiCommons {

  /**
    * Get a list of Trades for an Account
    * @param accountId  Account Identifier [required]
    * @param ids        List of Trade IDs to retrieve.
    * @param state      The state to filter the requested Trades by.
    * @param instrument The instrument to filter the requested Trades by.
    * @param count      The maximum number of Trades to return. [default=50, maximum=500]
    * @param beforeID   The maximum Trade ID to return. If not provided the most recent Trades in the Account are returned.
    * @return The list of Trades requested
    */
  def trades(accountId: AccountID,
             ids: Seq[TradeID],
             state: TradeState,
             instrument: InstrumentName,
             count: Int,
             beforeID: Option[TradeID]): \/[Error, TradesResponse] = {

    val params = Seq(
      Option(s"ids=${ids.map(_.value).mkString(",")}"),
      Option(s"state=$state"),
      Option(s"instrument=${instrument.value}"),
      Option(s"count=$count"),
      beforeID.map(s"beforeID=" + _)
    ).flatten.mkString("&")

    val url = s"$baseUrl/accounts/${accountId.value}/trades?$params"
    val content = Request
      .Get(url)
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[TradesResponse](content)
  }

}
