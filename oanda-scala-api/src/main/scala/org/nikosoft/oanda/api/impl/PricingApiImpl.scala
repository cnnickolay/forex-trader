package org.nikosoft.oanda.api.impl

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PricingHeartbeat}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.PricingApi
import org.nikosoft.oanda.api.`def`.PricingApi.{PricingOrHeartbeat, PricingResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}

private[api] object PricingApiImpl extends PricingApi with ApiCommons {

  /**
    * Get pricing information for a specified list of Instruments within an Account.
    *
    * @param accountId   Account Identifier [required]
    * @param instruments List of Instruments to get pricing for. [required]
    * @param since       Date/Time filter to apply to the returned prices. Only prices with a time later than this filter will be provided.
    * @return Pricing information has been successfully provided.
    */
  def pricing(accountId: AccountID, instruments: Seq[InstrumentName], since: Option[DateTime]): \/[Error, PricingResponse] = {
    val params = Seq(
      Option(s"instruments=${instruments.map(_.value).mkString(",")}"),
      since.map("since=" + _.value)
    ).flatten.mkString("&")

    val url = s"$baseUrl/accounts/${accountId.value}/pricing?$params"
    val content = Request
      .Get(url)
      .addHeader("Authorization", token)
      .execute()
      .returnContent()
      .toString

    handleRequest[PricingResponse](content)
  }

  /**
    * Get a stream of Account Prices starting from when the request is made. This pricing stream does not include every single price created for the Account, but instead will provide at most 4 prices per second (every 250 milliseconds) for each instrument being requested. If more than one price is created for an instrument during the 250 millisecond window, only the price in effect at the end of the window is sent. This means that during periods of rapid price movement, subscribers to this stream will not be sent every price. Pricing windows for different connections to the price stream are not all aligned in the same way (i.e. they are not all aligned to the top of the second). This means that during periods of rapid price movement, different subscribers may observe different prices depending on their alignment. Note: This endpoint is served by the streaming URLs.
    *
    * @param accountId   Account Identifier [required]
    * @param instruments List of Instruments to stream Prices for. [required]
    * @param snapshot    Flag that enables/disables the sending of a pricing snapshot when initially connecting to the stream. [default=True]
    * @return Connecting to the Price Stream was successful.
    *         The response body for the Pricing Stream uses chunked transfer encoding. Each chunk contains Price and/or PricingHeartbeat objects encoded as JSON. Each JSON object is serialized into a single line of text, and multiple objects found in the same chunk are separated by newlines. Heartbeats are sent every 5 seconds.
    */
  def pricingStream(accountId: AccountID, instruments: Seq[InstrumentName], snapshot: Boolean, terminate: => Boolean): BlockingQueue[PricingOrHeartbeat] = {
    val params = Seq(
      Option(s"instruments=${instruments.map(_.value).mkString(",")}"),
      Option(s"smooth=${if (snapshot) "True" else "False"}")
    ).flatten.mkString("&")

    val url = s"$streamUrl/accounts/${accountId.value}/pricing/stream?$params"

    val queue = new LinkedBlockingQueue[PricingOrHeartbeat]()

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
              handleRequest[Any](response.replaceAll("HEARTBEAT", "PRICING_HEARTBEAT")) match {
                case \/-(t: PricingHeartbeat) => queue.put(t.left)
                case \/-(t: Price) => queue.put(t.right)
                case -\/(err) => throw new RuntimeException(s"Error while parsing response $response\n$err")
              }
            }
        })
    }
    queue
  }
}
