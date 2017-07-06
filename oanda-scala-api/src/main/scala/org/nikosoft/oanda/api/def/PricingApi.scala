package org.nikosoft.oanda.api.`def`

import java.util.concurrent.BlockingQueue

import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PricingHeartbeat}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.PricingApi.{PricingOrHeartbeat, PricingResponse}

import scalaz.\/

object PricingApi {

  /**
    * @param prices The list of Price objects requested
    */
  case class PricingResponse(prices: Seq[Price])

  type PricingOrHeartbeat = \/[PricingHeartbeat, Price]

}

trait PricingApi {

  /**
    * Get pricing information for a specified list of Instruments within an Account.
    *
    * @param accountId   Account Identifier [required]
    * @param instruments List of Instruments to get pricing for. [required]
    * @param since       Date/Time filter to apply to the returned prices. Only prices with a time later than this filter will be provided.
    * @return Pricing information has been successfully provided.
    */
  def pricing(accountId: AccountID, instruments: Seq[InstrumentName], since: Option[DateTime] = None): \/[Error, PricingResponse]

  /**
    * Get a stream of Account Prices starting from when the request is made. This pricing stream does not include every single price created for the Account, but instead will provide at most 4 prices per second (every 250 milliseconds) for each instrument being requested. If more than one price is created for an instrument during the 250 millisecond window, only the price in effect at the end of the window is sent. This means that during periods of rapid price movement, subscribers to this stream will not be sent every price. Pricing windows for different connections to the price stream are not all aligned in the same way (i.e. they are not all aligned to the top of the second). This means that during periods of rapid price movement, different subscribers may observe different prices depending on their alignment. Note: This endpoint is served by the streaming URLs.
    *
    * @param accountId   Account Identifier [required]
    * @param instruments List of Instruments to stream Prices for. [required]
    * @param snapshot    Flag that enables/disables the sending of a pricing snapshot when initially connecting to the stream. [default=True]
    * @return Connecting to the Price Stream was successful.
    *         The response body for the Pricing Stream uses chunked transfer encoding. Each chunk contains Price and/or PricingHeartbeat objects encoded as JSON. Each JSON object is serialized into a single line of text, and multiple objects found in the same chunk are separated by newlines. Heartbeats are sent every 5 seconds.
    */
  def pricingStream(accountId: AccountID, instruments: Seq[InstrumentName], snapshot: Boolean, terminate: => Boolean): BlockingQueue[PricingOrHeartbeat]

}
