package org.nikosoft.oanda.api.`def`

import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountID
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderState.OrderState
import org.nikosoft.oanda.api.ApiModel.OrderModel.{Order, OrderID, OrderRequest, OrderSpecifier}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentName
import org.nikosoft.oanda.api.ApiModel.TransactionModel.{OrderCancelTransaction, OrderFillTransaction, Transaction, TransactionID}
import org.nikosoft.oanda.api.Errors.Error
import org.nikosoft.oanda.api.`def`.OrderApi.{CancelOrderResponse, CreateOrderRequest, CreateOrderResponse, OrdersResponse}

import scalaz.\/

object OrderApi {

  /**
    * @param orderCreateTransaction        The Transaction that created the Order specified by the request.
    * @param orderFillTransaction          The Transaction that filled the newly created Order. Only provided when the Order was immediately filled.
    * @param orderCancelTransaction        The Transaction that cancelled the newly created Order. Only provided when the Order was immediately cancelled.
    * @param orderReissueTransaction       The Transaction that reissues the Order. Only provided when the Order is configured to be reissued for its remaining units after a partial fill and the reissue was successful.
    * @param orderReissueRejectTransaction The Transaction that rejects the reissue of the Order. Only provided when the Order is configured to be reissued for its remaining units after a partial fill and the reissue was rejected.
    * @param relatedTransactionIDs         The IDs of all Transactions that were created while satisfying the request.
    * @param lastTransactionID             The ID of the most recent Transaction created for the Account
    */
  case class CreateOrderResponse(orderCreateTransaction: Option[Transaction],
                                 orderFillTransaction: Option[OrderFillTransaction],
                                 orderCancelTransaction: Option[OrderCancelTransaction],
                                 orderReissueTransaction: Option[Transaction],
                                 orderReissueRejectTransaction: Option[Transaction],
                                 relatedTransactionIDs: Seq[TransactionID],
                                 lastTransactionID: Option[TransactionID])

  /**
    * @param order Specification of the Order to create
    */
  case class CreateOrderRequest(order: OrderRequest)

  /**
    * The list of Orders requested
    *
    * @param orders            The list of Order detail objects
    * @param lastTransactionID The ID of the most recent Transaction created for the Account
    */
  case class OrdersResponse(orders: Seq[Order], lastTransactionID: Option[TransactionID])

  /**
    * @param orderCancelTransaction The Transaction that cancelled the Order
    * @param relatedTransactionIDs  The IDs of all Transactions that were created while satisfying the request.
    * @param lastTransactionID      The ID of the most recent Transaction created for the Account
    */
  case class CancelOrderResponse(orderCancelTransaction: Option[OrderCancelTransaction],
                                 relatedTransactionIDs: Seq[TransactionID],
                                 lastTransactionID: Option[TransactionID])

}

trait OrderApi {

  /**
    * Create an Order for an Account
    *
    * @param accountId Account Identifier [required]
    * @param order     Request body
    * @return The Order was created as specified
    */
  def createOrder(accountId: AccountID, order: CreateOrderRequest): \/[Error, CreateOrderResponse]

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
  def orders(accountId: AccountID, ids: Seq[OrderID] = Seq.empty, state: OrderState, instrument: Option[InstrumentName] = None, count: Int = 50, beforeID: Option[OrderID] = None): \/[Error, OrdersResponse]

  /**
    * Cancel a pending Order in an Account
    *
    * @param accountId      Account Identifier [required]
    * @param orderSpecifier The Order Specifier [required]
    * @return The Order was cancelled as specified
    */
  def cancelOrder(accountId: AccountID, orderSpecifier: OrderSpecifier): \/[Error, CancelOrderResponse]

}
