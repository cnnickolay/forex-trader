package org.nikosoft.oanda.api.`def`

import java.util.concurrent.BlockingQueue

import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.DateTime
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TransactionFilter.TransactionFilter
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{Transaction, TransactionHeartbeat, TransactionID}
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.TransactionApi.{TransactionOrHeartbeat, TransactionsIdRangeResponse, TransactionsResponse}

import scalaz.\/

object TransactionApi {

  /**
    * @param from              The starting time provided in the request.
    * @param to                The ending time provided in the request.
    * @param pageSize          The pageSize provided in the request
    * @param `type`            The Transaction-type filter provided in the request
    * @param count             The number of Transactions that are contained in the pages returned
    * @param pages             The list of URLs that represent idrange queries providing the data for each page in the query results
    * @param lastTransactionID The ID of the most recent Transaction created for the Account
    */
  case class TransactionsResponse(from: DateTime, to: DateTime, pageSize: Int, `type`: Seq[TransactionFilter], count: Int, pages: Seq[String], lastTransactionID: TransactionID)

  /**
    * @param transactions      The list of Transactions that satisfy the request.
    * @param lastTransactionID The ID of the most recent Transaction created for the Account
    */
  case class TransactionsIdRangeResponse(transactions: Seq[Transaction], lastTransactionID: TransactionID)

  type TransactionOrHeartbeat = \/[TransactionHeartbeat, Transaction]
}


trait TransactionApi {

  /**
    * Get a list of Transactions pages that satisfy a time-based Transaction query.
    *
    * @param accountId Account Identifier [required]
    * @param from      The starting time (inclusive) of the time range for the Transactions being queried. [default=Account Creation Time]
    * @param to        The ending time (inclusive) of the time range for the Transactions being queried. [default=Request Time]
    * @param pageSize  The number of Transactions to include in each page of the results. [default=100, maximum=1000]
    * @param `type`    A filter for restricting the types of Transactions to retreive.
    * @return The requested time range of Transaction pages are provided.
    */
  def transactions(accountId: AccountID, from: Option[DateTime] = None, to: Option[DateTime] = None, pageSize: Int = 100, `type`: Seq[TransactionFilter] = Seq.empty): \/[Error, TransactionsResponse]

  /**
    * Get a range of Transactions for an Account based on the Transaction IDs.
    *
    * @param accountId Account Identifier [required]
    * @param from      The starting Transacion ID (inclusive) to fetch. [required]
    * @param to        The ending Transaction ID (inclusive) to fetch. [required]
    * @param `type`    The filter that restricts the types of Transactions to retreive.
    * @return The requested time range of Transactions are provided.
    */
  def transactionsIdRange(accountId: AccountID, from: TransactionID, to: TransactionID, `type`: Seq[TransactionFilter] = Seq.empty): \/[Error, TransactionsIdRangeResponse]

  /**
    * Get a stream of Transactions for an Account starting from when the request is made.
    * Note: This endpoint is served by the streaming URLs.
    *
    * @param accountId Account Identifier [required]
    */
  def transactionsStream(accountId: AccountID, terminate: => Boolean): BlockingQueue[TransactionOrHeartbeat]

}
