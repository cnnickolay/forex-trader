package org.nikosoft.oanda.api.`def`

import org.nikosoft.oanda.api.ApiModel.AccountModel.{Account, AccountID, AccountProperties, AccountSummary}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{Instrument, InstrumentName}
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TransactionID
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.AccountsApi.{AccountInstrumentsResponse, AccountSummaryResponse, AccountsDetailsResponse, AccountsResponse}

import scalaz._

object AccountsApi {

  /**
    * @param accounts The list of Accounts the client is authorized to access and their associated properties.
    */
  case class AccountsResponse(accounts: Seq[AccountProperties])

  /**
    * @param account The full details of the requested Account.
    * @param lastTransactionID The ID of the most recent Transaction created for the Account.
    */
  case class AccountsDetailsResponse(account: Account, lastTransactionID: TransactionID)

  /**
    * @param account The summary of the requested Account.
    * @param lastTransactionID The ID of the most recent Transaction created for the Account.
    */
  case class AccountSummaryResponse(account: AccountSummary, lastTransactionID: TransactionID)

  /**
    * @param instruments The requested list of instruments.
    * @param lastTransactionID The ID of the most recent Transaction created for the Account.
    */
  case class AccountInstrumentsResponse(instruments: Seq[Instrument], lastTransactionID: TransactionID)

}

trait AccountsApi {

  /**
    * Get a list of all Accounts authorized for the provided token.
    * @return The list of authorized Accounts has been provided.
    */
  def accounts: \/[Error, AccountsResponse]

  /**
    * Get the full details for a single Account that a client has access to. Full pending Order, open Trade and open Position representations are provided.
    * @param accountId Account Identifier
    * @return The full Account details are provided
    */
  def accountDetails(accountId: AccountID): \/[Error, AccountsDetailsResponse]

  /**
    * Get a summary for a single Account that a client has access to.
    * @param accountId Account Identifier
    * @return The Account summary are provided
    */
  def accountSummary(accountId: AccountID): \/[Error, AccountSummaryResponse]

  /**
    * Get the list of tradeable instruments for the given Account. The list of tradeable instruments is dependent on the regulatory division that the Account is located in, thus should be the same for all Accounts owned by a single user.
    * @param accountId Account Identifier
    * @param instruments The list of tradeable instruments for the Account has been provided.
    * @return
    */
  def accountInstruments(accountId: AccountID, instruments: Seq[InstrumentName]): \/[Error, AccountInstrumentsResponse]

}
