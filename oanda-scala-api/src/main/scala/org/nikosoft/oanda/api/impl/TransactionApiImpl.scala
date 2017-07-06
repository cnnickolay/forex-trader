package org.nikosoft.oanda.api.impl

import java.io._
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.DateTime
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TransactionFilter.TransactionFilter
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{Transaction, TransactionHeartbeat, TransactionID}
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.TransactionApi
import org.nikosoft.oanda.api.`def`.TransactionApi.{TransactionOrHeartbeat, TransactionsIdRangeResponse, TransactionsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}

private[api] object TransactionApiImpl extends TransactionApi with ApiCommons {

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
  def transactions(accountId: AccountID, from: Option[DateTime], to: Option[DateTime], pageSize: Int, `type`: Seq[TransactionFilter]): \/[Error, TransactionsResponse] = {
    val params = Seq(
      from.map(s"from=" + _),
      to.map(s"to=" + _),
      Option(s"pageSize=$pageSize"),
      Option(`type`.map(_.toString).mkString(",")).filterNot(_.trim.isEmpty).map("type=" + _)
    ).flatten.mkString("&")

    val url = s"$baseUrl/accounts/${accountId.value}/transactions?$params"
    val content = Request
      .Get(url)
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[TransactionsResponse](content)
  }

  /**
    * Get a range of Transactions for an Account based on the Transaction IDs.
    *
    * @param accountId Account Identifier [required]
    * @param from      The starting Transacion ID (inclusive) to fetch. [required]
    * @param to        The ending Transaction ID (inclusive) to fetch. [required]
    * @param `type`    The filter that restricts the types of Transactions to retreive.
    * @return The requested time range of Transactions are provided.
    */
  def transactionsIdRange(accountId: AccountID, from: TransactionID, to: TransactionID, `type`: Seq[TransactionFilter]): \/[Error, TransactionsIdRangeResponse] = {
    val params = Seq(
      Option(s"from=${from.value}"),
      Option(s"to=${to.value}"),
      Option(`type`.map(_.toString).mkString(",")).filterNot(_.trim.isEmpty).map("type=" + _)
    ).flatten.mkString("&")

    val url = s"$baseUrl/accounts/${accountId.value}/transactions/idrange?$params"
    val content = Request
      .Get(url)
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[TransactionsIdRangeResponse](content)
  }

  /**
    * Get a stream of Transactions for an Account starting from when the request is made.
    * Note: This endpoint is served by the streaming URLs.
    *
    * @param accountId Account Identifier [required]
    */
  def transactionsStream(accountId: AccountID, terminate: => Boolean): BlockingQueue[TransactionOrHeartbeat] = {
    val url = s"$streamUrl/accounts/${accountId.value}/transactions/stream"

    val queue = new LinkedBlockingQueue[TransactionOrHeartbeat]()

    Future {
      Request
        .Get(url)
        .addHeader("Authorization", token)
        .execute()
        .handleResponse((response: HttpResponse) => {
          val input = response.getEntity.getContent
          val stream = new BufferedReader(new InputStreamReader(input))

          Iterator.continually(stream.readLine())
            .takeWhile(_ != None.orNull && !terminate)
            .foreach { response =>
              handleRequest[Any](response) match {
                case \/-(t: TransactionHeartbeat) => queue.put(t.left)
                case \/-(t: Transaction) => queue.put(t.right)
                case -\/(err) => throw new RuntimeException(s"Error while parsing response $response\n$err")
              }
            }
        })
    }
    queue
  }

}
