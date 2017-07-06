package org.nikosoft.oanda.api.impl

import org.apache.http.client.fluent.Request
import org.nikosoft.oanda.api.`def`.AccountsApi.{AccountInstrumentsResponse, AccountSummaryResponse, AccountsDetailsResponse, AccountsResponse}
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.`def`.AccountsApi

import scalaz.\/

/**
  * Created by Nikolai Cherkezishvili on 21/06/2017
  */
private[api] object AccountsApiImpl extends AccountsApi with ApiCommons {

  /**
    * Get a list of all Accounts authorized for the provided token.
    *
    * @return The list of authorized Accounts has been provided.
    */
  def accounts: \/[Error, AccountsResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts")
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[AccountsResponse](content)
  }

  /**
    * Get the full details for a single Account that a client has access to. Full pending Order, open Trade and open Position representations are provided.
    *
    * @param accountId Account Identifier
    * @return The full Account details are provided
    */
  def accountDetails(accountId: AccountID): \/[Error, AccountsDetailsResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts/${accountId.value}")
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[AccountsDetailsResponse](content)
  }

  /**
    * Get a summary for a single Account that a client has access to.
    *
    * @param accountId Account Identifier
    * @return The Account summary are provided
    */
  def accountSummary(accountId: AccountID): \/[Error, AccountSummaryResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts/${accountId.value}/summary")
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[AccountSummaryResponse](content)
  }

  /**
    * Get the list of tradeable instruments for the given Account. The list of tradeable instruments is dependent on the regulatory division that the Account is located in, thus should be the same for all Accounts owned by a single user.
    *
    * @param accountId   Account Identifier
    * @param instruments The list of tradeable instruments for the Account has been provided.
    * @return
    */
  def accountInstruments(accountId: AccountID, instruments: Seq[InstrumentName]): \/[Error, AccountInstrumentsResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts/${accountId.value}/instruments?instruments=${instruments.map(_.value).mkString(",")}")
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[AccountInstrumentsResponse](content)
  }
}
