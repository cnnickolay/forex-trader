package org.nikosoft.oanda.api.impl

import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.json4s.jackson.Serialization.write
import org.nikosoft.oanda.api.ApiCommons
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.{OrderID, OrderSpecifier}
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderState.OrderState
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.OrderApi
import org.nikosoft.oanda.api.`def`.OrderApi.{CancelOrderResponse, CreateOrderRequest, CreateOrderResponse, OrdersResponse}

import scala.util.Try
import scalaz.\/

private[api] object OrderApiImpl extends OrderApi with ApiCommons {

  /**
    * Create an Order for an Account
    *
    * @param accountId Account Identifier [required]
    * @param order     Request body
    * @return The Order was created as specified
    */
  def createOrder(accountId: AccountID, order: CreateOrderRequest): \/[Error, CreateOrderResponse] = {
    val jsonBody = write(order)

    val url = s"$baseUrl/accounts/${accountId.value}/orders"
    val content = Request
      .Post(url)
      .addHeader("Authorization", token)
      .bodyString(jsonBody, ContentType.APPLICATION_JSON)
      .execute()
      .returnResponse()

    handleRequest[CreateOrderResponse](content)
  }

  /**
    * Get a list of Orders for an Account
    *
    * @param accountId  Account Identifier [required]
    * @param ids        List of Order IDs to retrieve
    * @param state      The state to filter the requested Orders by
    * @param instrument The instrument to filter the requested orders by
    * @param count      The maximum number of Orders to return [default=50, maximum=500]
    * @param beforeID   The maximum Order ID to return. If not provided the most recent Orders in the Account are returned
    * @return The list of Orders requested
    */
  def orders(accountId: AccountID, ids: Seq[OrderID], state: OrderState, instrument: Option[InstrumentName], count: Int, beforeID: Option[OrderID]): \/[Error, OrdersResponse] = {
    val params = Seq(
      Option(s"ids=${ids.map(_.value).mkString(",")}"),
      Option(s"state=$state"),
      instrument.map(s"instrument=" + _),
      Option(s"count=$count"),
      beforeID.map(s"beforeID=" + _)
    ).flatten.mkString("&")

    val url = s"$baseUrl/accounts/${accountId.value}/orders?$params"
    val content = Request
      .Get(url)
      .addHeader("Authorization", token)
      .execute()
      .returnResponse()

    handleRequest[OrdersResponse](content)
  }

  /**
    * Cancel a pending Order in an Account
    *
    * @param accountId      Account Identifier [required]
    * @param orderSpecifier The Order Specifier [required]
    * @return The Order was cancelled as specified
    */
  def cancelOrder(accountId: AccountID, orderSpecifier: OrderSpecifier): \/[Error, CancelOrderResponse] = {
    val url = s"$baseUrl/accounts/${accountId.value}/orders/${orderSpecifier.value}/cancel"
    val content = Request
      .Put(url)
      .addHeader("Authorization", token)
      .execute()
      .returnResponse()

    handleRequest[CancelOrderResponse](content)
  }
}
