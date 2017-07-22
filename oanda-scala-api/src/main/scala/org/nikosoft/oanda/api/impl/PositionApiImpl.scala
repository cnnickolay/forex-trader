package org.nikosoft.oanda.api.impl

import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.json4s.native.Serialization._
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.PositionApi
import org.nikosoft.oanda.api.`def`.PositionApi.{ClosePositionRequest, ClosePositionResponse, PositionsResponse}

import scalaz.\/

private[api] object PositionApiImpl extends PositionApi with ApiCommons {
  /**
    * List all Positions for an Account. The Positions returned are for every instrument that has had a position during the lifetime of an the Account.
    *
    * @param accountId Account Identifier [required]
    * @return The Account’s Positions are provided.
    */
  def positions(accountId: AccountID): \/[Error, PositionsResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts/${accountId.value}/positions")
      .addHeader("Authorization", token)
      .execute()
      .returnResponse()

    handleRequest[PositionsResponse](content)
  }

  /**
    * List all open Positions for an Account. An open Position is a Position in an Account that currently has a Trade opened for it.
    *
    * @param accountId Account Identifier [required]
    * @return The Account’s open Positions are provided.
    */
  def openPositions(accountId: AccountID): \/[Error, PositionsResponse] = {
    val content = Request
      .Get(s"$baseUrl/accounts/${accountId.value}/openPositions")
      .addHeader("Authorization", token)
      .execute()
      .returnResponse()

    handleRequest[PositionsResponse](content)
  }

  /**
    * Closeout the open Position for a specific instrument in an Account.
    *
    * @param accountId  Account Identifier [required]
    * @param instrument Name of the Instrument [required]
    * @return The Position closeout request has been successfully processed.
    */
  def closePosition(accountId: AccountID, instrument: InstrumentName, closePositionRequest: ClosePositionRequest): \/[Error, ClosePositionResponse] = {
    val jsonBody = write(closePositionRequest)

    val url = s"$baseUrl/accounts/${accountId.value}/positions/${instrument.value}/close"
    val content = Request
      .Put(url)
      .addHeader("Authorization", token)
      .bodyString(jsonBody, ContentType.APPLICATION_JSON)
      .execute()
      .returnResponse()

    handleRequest[ClosePositionResponse](content)
  }

}
