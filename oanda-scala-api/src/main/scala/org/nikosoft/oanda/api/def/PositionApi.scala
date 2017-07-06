package org.nikosoft.oanda.api.`def`

import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PositionModel.Position
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel._
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.PositionApi.{ClosePositionRequest, ClosePositionResponse, PositionsResponse}

import scalaz.\/

object PositionApi {

  /**
    * @param positions         The list of Account Positions.
    * @param lastTransactionID The ID of the most recent Transaction created for the Account
    */
  case class PositionsResponse(positions: Seq[Position], lastTransactionID: TransactionID)

  /**
    * @param longUnits             Indication of how much of the long Position to closeout. Either the
    *                              string “ALL”, the string “NONE”, or a DecimalNumber representing how many
    *                              units of the long position to close using a PositionCloseout MarketOrder.
    *                              The units specified must always be positive.
    * @param longClientExtensions  The client extensions to add to the MarketOrder used to close the long
    *                              position.
    * @param shortUnits            Indication of how much of the short Position to closeout. Either the
    *                              string “ALL”, the string “NONE”, or a DecimalNumber representing how many
    *                              units of the short position to close using a PositionCloseout
    *                              MarketOrder. The units specified must always be positive.
    * @param shortClientExtensions The client extensions to add to the MarketOrder used to close the short
    *                              position.
    */
  case class ClosePositionRequest(longUnits: Option[String] = None, longClientExtensions: Option[ClientExtensions] = None, shortUnits: Option[String] = None, shortClientExtensions: Option[ClientExtensions] = None)

  /**
    * @param longOrderCreateTransaction  The MarketOrderTransaction created to close the long Position.
    * @param longOrderFillTransaction    OrderFill Transaction that closes the long Position
    * @param longOrderCancelTransaction  OrderCancel Transaction that cancels the MarketOrder created to close the long Position
    * @param shortOrderCreateTransaction The MarketOrderTransaction created to close the short Position.
    * @param shortOrderFillTransaction   OrderFill Transaction that closes the short Position
    * @param shortOrderCancelTransaction OrderCancel Transaction that cancels the MarketOrder created to close the short Position
    * @param relatedTransactionIDs       The IDs of all Transactions that were created while satisfying the request.
    * @param lastTransactionID           The ID of the most recent Transaction created for the Account
    */
  case class ClosePositionResponse(longOrderCreateTransaction: Option[MarketOrderTransaction],
                                   longOrderFillTransaction: Option[OrderFillTransaction],
                                   longOrderCancelTransaction: Option[OrderCancelTransaction],
                                   shortOrderCreateTransaction: Option[MarketOrderTransaction],
                                   shortOrderFillTransaction: Option[OrderFillTransaction],
                                   shortOrderCancelTransaction: Option[OrderCancelTransaction],
                                   relatedTransactionIDs: Seq[TransactionID],
                                   lastTransactionID: Option[TransactionID])

}

trait PositionApi {

  /**
    * List all Positions for an Account. The Positions returned are for every instrument that has had a position
    * during the lifetime of an the Account.
    *
    * @param accountId Account Identifier [required]
    * @return The Account’s Positions are provided.
    */
  def positions(accountId: AccountID): \/[Error, PositionsResponse]

  /**
    * List all open Positions for an Account. An open Position is a Position in an Account that currently has a Trade opened for it.
    *
    * @param accountId Account Identifier [required]
    * @return The Account’s open Positions are provided.
    */
  def openPositions(accountId: AccountID): \/[Error, PositionsResponse]

  /**
    * Closeout the open Position for a specific instrument in an Account.
    *
    * @param accountId  Account Identifier [required]
    * @param instrument Name of the Instrument [required]
    * @param closePositionRequest request body
    * @return The Position closeout request has been successfully processed.
    */
  def closePosition(accountId: AccountID, instrument: InstrumentName, closePositionRequest: ClosePositionRequest): \/[Error, ClosePositionResponse]

}
