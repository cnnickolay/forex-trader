package org.nikosoft.oanda.api

import org.joda.time
import org.joda.time.DateTime
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountFinancingMode.AccountFinancingMode
import org.nikosoft.oanda.api.ApiModel.TransactionModel._
import org.nikosoft.oanda.api.ApiModel.AccountModel._
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderPositionFill.OrderPositionFill
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderState.OrderState
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderTriggerCondition.OrderTriggerCondition
import org.nikosoft.oanda.api.ApiModel.OrderModel.OrderType.OrderType
import org.nikosoft.oanda.api.ApiModel.OrderModel.TimeInForce.TimeInForce
import org.nikosoft.oanda.api.ApiModel.OrderModel._
import org.nikosoft.oanda.api.ApiModel.TradeModel._
import org.nikosoft.oanda.api.ApiModel.PositionModel._
import org.nikosoft.oanda.api.ApiModel.PricingModel.PriceStatus.PriceStatus
import org.nikosoft.oanda.api.ApiModel.PricingModel._
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentType.InstrumentType
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel._
import org.nikosoft.oanda.api.ApiModel.TradeModel.TradeState.TradeState
import org.nikosoft.oanda.api.ApiModel.TransactionModel.FundingReason.FundingReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.LimitOrderReason.LimitOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.MarketIfTouchedOrderReason.MarketIfTouchedOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.MarketOrderMarginCloseoutReason.MarketOrderMarginCloseoutReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.MarketOrderReason.MarketOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.OrderCancelReason.OrderCancelReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.OrderFillReason.OrderFillReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.StopLossOrderReason.StopLossOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.StopOrderReason.StopOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TakeProfitOrderReason.TakeProfitOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TrailingStopLossOrderReason.TrailingStopLossOrderReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TransactionRejectReason.TransactionRejectReason
import org.nikosoft.oanda.api.ApiModel.TransactionModel.TransactionType.TransactionType

/**
  * This Model was generated from OANDA v20 definitions http://developer.oanda.com/rest-live-v20/order-ep/
  */
object ApiModel {

  object TransactionModel {

    /**
      * The base Transaction specification. Specifies properties that are common between all Transaction.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction.
      */
    abstract class Transaction(id: TransactionID,
                               time: DateTime,
                               userID: Int,
                               accountID: AccountID,
                               batchID: TransactionID,
                               requestID: Option[RequestID],
                               `type`: TransactionType)

    /**
      * A CreateTransaction represents the creation of an Account.
      *
      * @param id            The Transaction’s Identifier.
      * @param time          The date/time when the Transaction was created.
      * @param userID        The ID of the user that initiated the creation of the Transaction.
      * @param accountID     The ID of the Account the Transaction was created for.
      * @param batchID       The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID     The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type          The Type of the Transaction. Always set to "CREATE" in a CreateTransaction.
      * @param divisionID    The ID of the Division that the Account is in
      * @param siteID        The ID of the Site that the Account was created at
      * @param accountUserID The ID of the user that the Account was created for
      * @param accountNumber The number of the Account within the site/division/user
      * @param homeCurrency  The home currency of the Account
      */
    case class CreateTransaction(id: TransactionID,
                                 time: DateTime,
                                 userID: Int,
                                 accountID: AccountID,
                                 batchID: TransactionID,
                                 requestID: Option[RequestID],
                                 `type`: TransactionType = TransactionType.CREATE,
                                 divisionID: Int,
                                 siteID: Int,
                                 accountUserID: Int,
                                 accountNumber: Int,
                                 homeCurrency: Currency
                                ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A CloseTransaction represents the closing of an Account.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "CLOSE" in a CloseTransaction.
      */
    case class CloseTransaction(id: TransactionID,
                                time: DateTime,
                                userID: Int,
                                accountID: AccountID,
                                batchID: TransactionID,
                                requestID: Option[RequestID],
                                `type`: TransactionType = TransactionType.CLOSE
                               ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A ReopenTransaction represents the re-opening of a closed Account.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "REOPEN" in a ReopenTransaction.
      */
    case class ReopenTransaction(id: TransactionID,
                                 time: DateTime,
                                 userID: Int,
                                 accountID: AccountID,
                                 batchID: TransactionID,
                                 requestID: Option[RequestID],
                                 `type`: TransactionType = TransactionType.REOPEN
                                ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A ClientConfigureTransaction represents the configuration of an Account by a client.
      *
      * @param id         The Transaction’s Identifier.
      * @param time       The date/time when the Transaction was created.
      * @param userID     The ID of the user that initiated the creation of the Transaction.
      * @param accountID  The ID of the Account the Transaction was created for.
      * @param batchID    The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID  The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type       The Type of the Transaction. Always set to "CLIENT_CONFIGURE" in a ClientConfigureTransaction.
      * @param alias      The client-provided alias for the Account.
      * @param marginRate The margin rate override for the Account.
      */
    case class ClientConfigureTransaction(id: TransactionID,
                                          time: DateTime,
                                          userID: Int,
                                          accountID: AccountID,
                                          batchID: TransactionID,
                                          requestID: Option[RequestID],
                                          `type`: TransactionType = TransactionType.CLIENT_CONFIGURE,
                                          alias: String,
                                          marginRate: Double
                                         ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A ClientConfigureRejectTransaction represents the reject of configuration of an Account by a client.
      *
      * @param id           The Transaction’s Identifier.
      * @param time         The date/time when the Transaction was created.
      * @param userID       The ID of the user that initiated the creation of the Transaction.
      * @param accountID    The ID of the Account the Transaction was created for.
      * @param batchID      The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID    The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type         The Type of the Transaction. Always set to "CLIENT_CONFIGURE_REJECT" in a ClientConfigureRejectTransaction.
      * @param alias        The client-provided alias for the Account.
      * @param rejectReason The margin rate override for the Account.
      */
    case class ClientConfigureRejectTransaction(id: TransactionID,
                                                time: DateTime,
                                                userID: Int,
                                                accountID: AccountID,
                                                batchID: TransactionID,
                                                requestID: Option[RequestID],
                                                `type`: TransactionType = TransactionType.CLIENT_CONFIGURE_REJECT,
                                                alias: String,
                                                marginRate: Double,
                                                rejectReason: TransactionRejectReason
                                               ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TransferFundsTransaction represents the transfer of funds in/out of an Account.
      *
      * @param time           The Transaction’s Identifier.
      * @param userID         The date/time when the Transaction was created.
      * @param accountID      The ID of the user that initiated the creation of the Transaction.
      * @param batchID        The ID of the Account the Transaction was created for.
      * @param requestID      The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type           The Type of the Transaction. Always set to "TRANSFER_FUNDS" in a TransferFundsTransaction.
      * @param amount         The amount to deposit/withdraw from the Account in the Account’s home currency. A positive value indicates a deposit, a negative value indicates a withdrawal.
      * @param fundingReason  The reason that an Account is being funded.
      * @param accountBalance The Account’s balance after funds are transferred.
      */
    case class TransferFundsTransaction(id: TransactionID,
                                        time: DateTime,
                                        userID: Int,
                                        accountID: AccountID,
                                        batchID: TransactionID,
                                        requestID: Option[RequestID],
                                        `type`: TransactionType = TransactionType.TRANSFER_FUNDS,
                                        amount: AccountUnits,
                                        fundingReason: FundingReason,
                                        accountBalance: AccountUnits
                                       ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TransferFundsRejectTransaction represents the rejection of the transfer of funds in/out of an Account.
      *
      * @param id            The Transaction’s Identifier.
      * @param time          The date/time when the Transaction was created.
      * @param userID        The ID of the user that initiated the creation of the Transaction.
      * @param accountID     The ID of the Account the Transaction was created for.
      * @param batchID       The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID     The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type          The Type of the Transaction. Always set to "TRANSFER_FUNDS_REJECT" in a TransferFundsRejectTransaction.
      * @param amount        The amount to deposit/withdraw from the Account in the Account’s home currency. A positive value indicates a deposit, a negative value indicates a withdrawal.
      * @param fundingReason The reason that an Account is being funded.
      * @param rejectReason  The reason that the Reject Transaction was created
      */
    case class TransferFundsRejectTransaction(id: TransactionID,
                                              time: DateTime,
                                              userID: Int,
                                              accountID: AccountID,
                                              batchID: TransactionID,
                                              requestID: Option[RequestID],
                                              `type`: TransactionType = TransactionType.TRANSFER_FUNDS_REJECT,
                                              amount: AccountUnits,
                                              fundingReason: FundingReason,
                                              rejectReason: TransactionRejectReason
                                             ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarketOrderTransaction represents the creation of a Market Order in the user’s account. A Market Order is an Order that is filled immediately at the current market price. Market Orders can be specialized when they are created to accomplish a specific task: to close a Trade, to closeout a Position or to particiate in in a Margin closeout.
      *
      * @param id                     The Transaction’s Identifier.
      * @param time                   The date/time when the Transaction was created.
      * @param userID                 The ID of the user that initiated the creation of the Transaction.
      * @param accountID              The ID of the Account the Transaction was created for.
      * @param batchID                The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID              The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                   The Type of the Transaction. Always set to "MARKET_ORDER" in a MarketOrderTransaction.
      * @param instrument             The Market Order’s Instrument.
      * @param units                  The quantity requested to be filled by the Market Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param timeInForce            The time-in-force requested for the Market Order. Restricted to FOK or IOC for a MarketOrder.
      * @param priceBound             The worst price that the client is willing to have the Market Order filled at.
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param tradeClose             Details of the Trade requested to be closed, only provided when the Market Order is being used to explicitly close a Trade.
      * @param longPositionCloseout   Details of the long Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a long Position.
      * @param shortPositionCloseout  Details of the short Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a short Position.
      * @param marginCloseout         Details of the Margin Closeout that this Market Order was created for
      * @param delayedTradeClose      Details of the delayed Trade close that this Market Order was created for
      * @param reason                 The reason that the Market Order was created
      * @param clientExtensions       Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill       The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill         The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      */
    case class MarketOrderTransaction(
                                       id: TransactionID,
                                       time: DateTime,
                                       userID: Int,
                                       accountID: AccountID,
                                       batchID: TransactionID,
                                       requestID: Option[RequestID],
                                       `type`: TransactionType = TransactionType.MARKET_ORDER,
                                       instrument: InstrumentName,
                                       units: Double,
                                       timeInForce: TimeInForce,
                                       priceBound: PriceValue,
                                       positionFill: OrderPositionFill,
                                       tradeClose: MarketOrderTradeClose,
                                       longPositionCloseout: MarketOrderPositionCloseout,
                                       shortPositionCloseout: MarketOrderPositionCloseout,
                                       marginCloseout: MarketOrderMarginCloseout,
                                       delayedTradeClose: MarketOrderDelayedTradeClose,
                                       reason: MarketOrderReason,
                                       clientExtensions: Option[ClientExtensions],
                                       takeProfitOnFill: Option[TakeProfitDetails],
                                       stopLossOnFill: Option[StopLossDetails],
                                       trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                       tradeclientExtensions: Option[ClientExtensions]
                                     ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarketOrderRejectTransaction represents the rejection of the creation of a Market Order.
      *
      * @param id                     The Transaction’s Identifier.
      * @param time                   The date/time when the Transaction was created.
      * @param userID                 The ID of the user that initiated the creation of the Transaction.
      * @param accountID              The ID of the Account the Transaction was created for.
      * @param batchID                The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID              The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                   The Type of the Transaction. Always set to "MARKET_ORDER_REJECT" in a MarketOrderRejectTransaction.
      * @param instrument             The Market Order’s Instrument.
      * @param units                  The quantity requested to be filled by the Market Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param timeInForce            The time-in-force requested for the Market Order. Restricted to FOK or IOC for a MarketOrder.
      * @param priceBound             The worst price that the client is willing to have the Market Order filled at.
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param tradeClose             Details of the Trade requested to be closed, only provided when the Market Order is being used to explicitly close a Trade.
      * @param longPositionCloseout   Details of the long Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a long Position.
      * @param shortPositionCloseout  Details of the short Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a short Position.
      * @param marginCloseout         Details of the Margin Closeout that this Market Order was created for
      * @param delayedTradeClose      Details of the delayed Trade close that this Market Order was created for
      * @param reason                 The reason that the Market Order was created
      * @param clientExtensions       Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill       The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill         The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param rejectReason           The reason that the Reject Transaction was created
      */
    case class MarketOrderRejectTransaction(
                                             id: TransactionID,
                                             time: DateTime,
                                             userID: Int,
                                             accountID: AccountID,
                                             batchID: TransactionID,
                                             requestID: Option[RequestID],
                                             `type`: TransactionType = TransactionType.MARKET_ORDER_REJECT,
                                             instrument: InstrumentName,
                                             units: Double,
                                             timeInForce: TimeInForce,
                                             priceBound: PriceValue,
                                             positionFill: OrderPositionFill,
                                             tradeClose: MarketOrderTradeClose,
                                             longPositionCloseout: MarketOrderPositionCloseout,
                                             shortPositionCloseout: MarketOrderPositionCloseout,
                                             marginCloseout: MarketOrderMarginCloseout,
                                             delayedTradeClose: MarketOrderDelayedTradeClose,
                                             reason: MarketOrderReason,
                                             clientExtensions: Option[ClientExtensions],
                                             takeProfitOnFill: Option[TakeProfitDetails],
                                             stopLossOnFill: Option[StopLossDetails],
                                             trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                             tradeclientExtensions: Option[ClientExtensions],
                                             rejectReason: TransactionRejectReason
                                           ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A LimitOrderTransaction represents the creation of a Limit Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "LIMIT_ORDER" in a LimitOrderTransaction.
      * @param instrument              The Limit Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Limit Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Limit Order. The Limit Order will only be filled by a market price that is equal to or better than this price.
      * @param timeInForce             The time-in-force requested for the Limit Order.
      * @param gtdTime                 The date/time when the Limit Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Limit Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class LimitOrderTransaction(
                                      id: TransactionID,
                                      time: DateTime,
                                      userID: Int,
                                      accountID: AccountID,
                                      batchID: TransactionID,
                                      requestID: Option[RequestID],
                                      `type`: TransactionType = TransactionType.LIMIT_ORDER,
                                      instrument: InstrumentName,
                                      units: Double,
                                      price: PriceValue,
                                      timeInForce: TimeInForce,
                                      gtdTime: Option[DateTime],
                                      positionFill: OrderPositionFill,
                                      triggerCondition: OrderTriggerCondition,
                                      reason: LimitOrderReason,
                                      clientExtensions: Option[ClientExtensions],
                                      takeProfitOnFill: Option[TakeProfitDetails],
                                      stopLossOnFill: Option[StopLossDetails],
                                      trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                      tradeclientExtensions: Option[ClientExtensions],
                                      replacesOrderID: Option[OrderID],
                                      cancellingTransactionID: Option[TransactionID]
                                    ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A LimitOrderRejectTransaction represents the rejection of the creation of a Limit Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "LIMIT_ORDER_REJECT" in a LimitOrderRejectTransaction.
      * @param instrument              The Limit Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Limit Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Limit Order. The Limit Order will only be filled by a market price that is equal to or better than this price.
      * @param timeInForce             The time-in-force requested for the Limit Order.
      * @param gtdTime                 The date/time when the Limit Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Limit Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class LimitOrderRejectTransaction(
                                            id: TransactionID,
                                            time: DateTime,
                                            userID: Int,
                                            accountID: AccountID,
                                            batchID: TransactionID,
                                            requestID: Option[RequestID],
                                            `type`: TransactionType = TransactionType.LIMIT_ORDER_REJECT,
                                            instrument: InstrumentName,
                                            units: Double,
                                            price: PriceValue,
                                            timeInForce: TimeInForce,
                                            gtdTime: Option[DateTime],
                                            positionFill: OrderPositionFill,
                                            triggerCondition: OrderTriggerCondition,
                                            reason: LimitOrderReason,
                                            clientExtensions: Option[ClientExtensions],
                                            takeProfitOnFill: Option[TakeProfitDetails],
                                            stopLossOnFill: Option[StopLossDetails],
                                            trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                            tradeclientExtensions: Option[ClientExtensions],
                                            intendedReplacesOrderID: Option[OrderID],
                                            rejectReason: TransactionRejectReason
                                          ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A StopOrderTransaction represents the creation of a Stop Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "STOP_ORDER" in a StopOrderTransaction.
      * @param instrument              The Stop Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Stop Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Stop Order. The Stop Order will only be filled by a market price that is equal to or worse than this price.
      * @param priceBound              The worst market price that may be used to fill this Stop Order. If the market gaps and crosses through both the price and the priceBound, the Stop Order will be cancelled instead of being filled.
      * @param timeInForce             The time-in-force requested for the Stop Order.
      * @param gtdTime                 The date/time when the Stop Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Stop Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class StopOrderTransaction(
                                     id: TransactionID,
                                     time: DateTime,
                                     userID: Int,
                                     accountID: AccountID,
                                     batchID: TransactionID,
                                     requestID: Option[RequestID],
                                     `type`: TransactionType = TransactionType.STOP_ORDER,
                                     instrument: InstrumentName,
                                     units: Double,
                                     price: PriceValue,
                                     priceBound: PriceValue,
                                     timeInForce: TimeInForce,
                                     gtdTime: Option[DateTime],
                                     positionFill: OrderPositionFill,
                                     triggerCondition: OrderTriggerCondition,
                                     reason: StopOrderReason,
                                     clientExtensions: Option[ClientExtensions],
                                     takeProfitOnFill: Option[TakeProfitDetails],
                                     stopLossOnFill: Option[StopLossDetails],
                                     trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                     tradeclientExtensions: Option[ClientExtensions],
                                     replacesOrderID: Option[OrderID],
                                     cancellingTransactionID: Option[TransactionID]
                                   ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A StopOrderRejectTransaction represents the rejection of the creation of a Stop Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "STOP_ORDER_REJECT" in a StopOrderRejectTransaction.
      * @param instrument              The Stop Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Stop Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Stop Order. The Stop Order will only be filled by a market price that is equal to or worse than this price.
      * @param priceBound              The worst market price that may be used to fill this Stop Order. If the market gaps and crosses through both the price and the priceBound, the Stop Order will be cancelled instead of being filled.
      * @param timeInForce             The time-in-force requested for the Stop Order.
      * @param gtdTime                 The date/time when the Stop Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Stop Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class StopOrderRejectTransaction(id: TransactionID,
                                          time: DateTime,
                                          userID: Int,
                                          accountID: AccountID,
                                          batchID: TransactionID,
                                          requestID: Option[RequestID],
                                          `type`: TransactionType = TransactionType.STOP_ORDER_REJECT,
                                          instrument: InstrumentName,
                                          units: Double,
                                          price: PriceValue,
                                          priceBound: PriceValue,
                                          timeInForce: TimeInForce,
                                          gtdTime: Option[DateTime],
                                          positionFill: OrderPositionFill,
                                          triggerCondition: OrderTriggerCondition,
                                          reason: StopOrderReason,
                                          clientExtensions: Option[ClientExtensions],
                                          takeProfitOnFill: Option[TakeProfitDetails],
                                          stopLossOnFill: Option[StopLossDetails],
                                          trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                          tradeclientExtensions: Option[ClientExtensions],
                                          intendedReplacesOrderID: Option[OrderID],
                                          rejectReason: TransactionRejectReason
                                         ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarketIfTouchedOrderTransaction represents the creation of a MarketIfTouched Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "MARKET_IF_TOUCHED_ORDER" in a MarketIfTouchedOrderTransaction.
      * @param instrument              The MarketIfTouched Order’s Instrument.
      * @param units                   The quantity requested to be filled by the MarketIfTouched Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the MarketIfTouched Order. The MarketIfTouched Order will only be filled by a market price that crosses this price from the direction of the market price at the time when the Order was created (the initialMarketPrice). Depending on the value of the Order’s price and initialMarketPrice, the MarketIfTouchedOrder will behave like a Limit or a Stop Order.
      * @param priceBound              The worst market price that may be used to fill this MarketIfTouched Order.
      * @param timeInForce             The time-in-force requested for the MarketIfTouched Order. Restricted to "GTC", "GFD" and "GTD" for MarketIfTouched Orders.
      * @param gtdTime                 The date/time when the MarketIfTouched Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Market-if-touched Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class MarketIfTouchedOrderTransaction(id: TransactionID,
                                               time: DateTime,
                                               userID: Int,
                                               accountID: AccountID,
                                               batchID: TransactionID,
                                               requestID: Option[RequestID],
                                               `type`: TransactionType = TransactionType.MARKET_IF_TOUCHED_ORDER,
                                               instrument: InstrumentName,
                                               units: Double,
                                               price: PriceValue,
                                               priceBound: PriceValue,
                                               timeInForce: TimeInForce,
                                               gtdTime: Option[DateTime],
                                               positionFill: OrderPositionFill,
                                               triggerCondition: OrderTriggerCondition,
                                               reason: MarketIfTouchedOrderReason,
                                               clientExtensions: Option[ClientExtensions],
                                               takeProfitOnFill: Option[TakeProfitDetails],
                                               stopLossOnFill: Option[StopLossDetails],
                                               trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                               tradeclientExtensions: Option[ClientExtensions],
                                               replacesOrderID: Option[OrderID],
                                               cancellingTransactionID: Option[TransactionID]
                                              ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarketIfTouchedOrderRejectTransaction represents the rejection of the creation of a MarketIfTouched Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "MARKET_IF_TOUCHED_ORDER_REJECT" in a MarketIfTouchedOrderRejectTransaction.
      * @param instrument              The MarketIfTouched Order’s Instrument.
      * @param units                   The quantity requested to be filled by the MarketIfTouched Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the MarketIfTouched Order. The MarketIfTouched Order will only be filled by a market price that crosses this price from the direction of the market price at the time when the Order was created (the initialMarketPrice). Depending on the value of the Order’s price and initialMarketPrice, the MarketIfTouchedOrder will behave like a Limit or a Stop Order.
      * @param priceBound              The worst market price that may be used to fill this MarketIfTouched Order.
      * @param timeInForce             The time-in-force requested for the MarketIfTouched Order. Restricted to "GTC", "GFD" and "GTD" for MarketIfTouched Orders.
      * @param gtdTime                 The date/time when the MarketIfTouched Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Market-if-touched Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param takeProfitOnFill        The specification of the Take Profit Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param stopLossOnFill          The specification of the Stop Loss Order that should be created for a Trade opened when the Order is filled (if such a Trade is created).
      * @param trailingStopLossOnFill  The specification of the Trailing Stop Loss Order that should be created for a Trade that is opened when the Order is filled (if such a Trade is created).
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created).  Do not set, modify, delete tradeClientExtensions if your account is associated with MT4.
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class MarketIfTouchedOrderRejectTransaction(id: TransactionID,
                                                     time: DateTime,
                                                     userID: Int,
                                                     accountID: AccountID,
                                                     batchID: TransactionID,
                                                     requestID: Option[RequestID],
                                                     `type`: TransactionType = TransactionType.MARKET_IF_TOUCHED_ORDER_REJECT,
                                                     instrument: InstrumentName,
                                                     units: Double,
                                                     price: PriceValue,
                                                     priceBound: PriceValue,
                                                     timeInForce: TimeInForce,
                                                     gtdTime: Option[DateTime],
                                                     positionFill: OrderPositionFill,
                                                     triggerCondition: OrderTriggerCondition,
                                                     reason: MarketIfTouchedOrderReason,
                                                     clientExtensions: Option[ClientExtensions],
                                                     takeProfitOnFill: Option[TakeProfitDetails],
                                                     stopLossOnFill: Option[StopLossDetails],
                                                     trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                                     tradeclientExtensions: Option[ClientExtensions],
                                                     intendedReplacesOrderID: Option[OrderID],
                                                     rejectReason: TransactionRejectReason
                                                    ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TakeProfitOrderTransaction represents the creation of a TakeProfit Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "TAKE_PROFIT_ORDER" in a TakeProfitOrderTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the TakeProfit Order. The associated Trade will be closed by a market price that is equal to or better than this threshold.
      * @param timeInForce             The time-in-force requested for the TakeProfit Order. Restricted to "GTC", "GFD" and "GTD" for TakeProfit Orders.
      * @param gtdTime                 The date/time when the TakeProfit Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Take Profit Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class TakeProfitOrderTransaction(id: TransactionID,
                                          time: DateTime,
                                          userID: Int,
                                          accountID: AccountID,
                                          batchID: TransactionID,
                                          requestID: Option[RequestID],
                                          `type`: TransactionType = TransactionType.TAKE_PROFIT_ORDER,
                                          tradeID: TradeID,
                                          clientTradeID: ClientID,
                                          price: PriceValue,
                                          timeInForce: TimeInForce,
                                          gtdTime: Option[DateTime],
                                          triggerCondition: OrderTriggerCondition,
                                          reason: TakeProfitOrderReason,
                                          clientExtensions: Option[ClientExtensions],
                                          orderFillTransactionID: TransactionID,
                                          replacesOrderID: Option[OrderID],
                                          cancellingTransactionID: Option[TransactionID]
                                         ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TakeProfitOrderRejectTransaction represents the rejection of the creation of a TakeProfit Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "TAKE_PROFIT_ORDER_REJECT" in a TakeProfitOrderRejectTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the TakeProfit Order. The associated Trade will be closed by a market price that is equal to or better than this threshold.
      * @param timeInForce             The time-in-force requested for the TakeProfit Order. Restricted to "GTC", "GFD" and "GTD" for TakeProfit Orders.
      * @param gtdTime                 The date/time when the TakeProfit Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Take Profit Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class TakeProfitOrderRejectTransaction(id: TransactionID,
                                                time: DateTime,
                                                userID: Int,
                                                accountID: AccountID,
                                                batchID: TransactionID,
                                                requestID: Option[RequestID],
                                                `type`: TransactionType = TransactionType.TAKE_PROFIT_ORDER_REJECT,
                                                tradeID: TradeID,
                                                clientTradeID: ClientID,
                                                price: PriceValue,
                                                timeInForce: TimeInForce,
                                                gtdTime: Option[DateTime],
                                                triggerCondition: OrderTriggerCondition,
                                                reason: TakeProfitOrderReason,
                                                clientExtensions: Option[ClientExtensions],
                                                orderFillTransactionID: TransactionID,
                                                intendedReplacesOrderID: Option[OrderID],
                                                rejectReason: TransactionRejectReason
                                               ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A StopLossOrderTransaction represents the creation of a StopLoss Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "STOP_LOSS_ORDER" in a StopLossOrderTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the StopLoss Order. The associated Trade will be closed by a market price that is equal to or worse than this threshold.
      * @param timeInForce             The time-in-force requested for the StopLoss Order. Restricted to "GTC", "GFD" and "GTD" for StopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Stop Loss Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class StopLossOrderTransaction(id: TransactionID,
                                        time: DateTime,
                                        userID: Int,
                                        accountID: AccountID,
                                        batchID: TransactionID,
                                        requestID: Option[RequestID],
                                        `type`: TransactionType = TransactionType.STOP_LOSS_ORDER,
                                        tradeID: TradeID,
                                        clientTradeID: ClientID,
                                        price: PriceValue,
                                        timeInForce: TimeInForce,
                                        gtdTime: Option[DateTime],
                                        triggerCondition: OrderTriggerCondition,
                                        reason: StopLossOrderReason,
                                        clientExtensions: Option[ClientExtensions],
                                        orderFillTransactionID: TransactionID,
                                        replacesOrderID: Option[OrderID],
                                        cancellingTransactionID: Option[TransactionID]
                                       ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A StopLossOrderRejectTransaction represents the rejection of the creation of a StopLoss Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "STOP_LOSS_ORDER_REJECT" in a StopLossOrderRejectTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the StopLoss Order. The associated Trade will be closed by a market price that is equal to or worse than this threshold.
      * @param timeInForce             The time-in-force requested for the StopLoss Order. Restricted to "GTC", "GFD" and "GTD" for StopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Stop Loss Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class StopLossOrderRejectTransaction(id: TransactionID,
                                              time: DateTime,
                                              userID: Int,
                                              accountID: AccountID,
                                              batchID: TransactionID,
                                              requestID: Option[RequestID],
                                              `type`: TransactionType = TransactionType.STOP_LOSS_ORDER_REJECT,
                                              tradeID: TradeID,
                                              clientTradeID: ClientID,
                                              price: PriceValue,
                                              timeInForce: TimeInForce,
                                              gtdTime: Option[DateTime],
                                              triggerCondition: OrderTriggerCondition,
                                              reason: StopLossOrderReason,
                                              clientExtensions: Option[ClientExtensions],
                                              orderFillTransactionID: TransactionID,
                                              intendedReplacesOrderID: Option[OrderID],
                                              rejectReason: TransactionRejectReason
                                             ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TrailingStopLossOrderTransaction represents the creation of a TrailingStopLoss Order in the user’s Account.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "TRAILING_STOP_LOSS_ORDER" in a TrailingStopLossOrderTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param distance                The price distance specified for the TrailingStopLoss Order.
      * @param timeInForce             The time-in-force requested for the TrailingStopLoss Order. Restricted to "GTC", "GFD" and "GTD" for TrailingStopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Trailing Stop Loss Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param replacesOrderID         The ID of the Order that this Order replaces (only provided if this Order replaces an existing Order).
      * @param cancellingTransactionID The ID of the Transaction that cancels the replaced Order (only provided if this Order replaces an existing Order).
      */
    case class TrailingStopLossOrderTransaction(id: TransactionID,
                                                time: DateTime,
                                                userID: Int,
                                                accountID: AccountID,
                                                batchID: TransactionID,
                                                requestID: Option[RequestID],
                                                `type`: TransactionType = TransactionType.TRAILING_STOP_LOSS_ORDER,
                                                tradeID: TradeID,
                                                clientTradeID: ClientID,
                                                distance: PriceValue,
                                                timeInForce: TimeInForce,
                                                gtdTime: Option[DateTime],
                                                triggerCondition: OrderTriggerCondition,
                                                reason: TrailingStopLossOrderReason,
                                                clientExtensions: Option[ClientExtensions],
                                                orderFillTransactionID: TransactionID,
                                                replacesOrderID: Option[OrderID],
                                                cancellingTransactionID: Option[TransactionID]
                                               ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TrailingStopLossOrderRejectTransaction represents the rejection of the creation of a TrailingStopLoss Order.
      *
      * @param id                      The Transaction’s Identifier.
      * @param time                    The date/time when the Transaction was created.
      * @param userID                  The ID of the user that initiated the creation of the Transaction.
      * @param accountID               The ID of the Account the Transaction was created for.
      * @param batchID                 The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID               The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                    The Type of the Transaction. Always set to "TRAILING_STOP_LOSS_ORDER_REJECT" in a TrailingStopLossOrderRejectTransaction.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param distance                The price distance specified for the TrailingStopLoss Order.
      * @param timeInForce             The time-in-force requested for the TrailingStopLoss Order. Restricted to "GTC", "GFD" and "GTD" for TrailingStopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param reason                  The reason that the Trailing Stop Loss Order was initiated
      * @param clientExtensions        Client Extensions to add to the Order (only provided if the Order is being created with client extensions).
      * @param orderFillTransactionID  The ID of the OrderFill Transaction that caused this Order to be created (only provided if this Order was created automatically when another Order was filled).
      * @param intendedReplacesOrderID The ID of the Order that this Order was intended to replace (only provided if this Order was intended to replace an existing Order).
      * @param rejectReason            The reason that the Reject Transaction was created
      */
    case class TrailingStopLossOrderRejectTransaction(id: TransactionID,
                                                      time: DateTime,
                                                      userID: Int,
                                                      accountID: AccountID,
                                                      batchID: TransactionID,
                                                      requestID: Option[RequestID],
                                                      `type`: TransactionType = TransactionType.TRAILING_STOP_LOSS_ORDER_REJECT,
                                                      tradeID: TradeID,
                                                      clientTradeID: ClientID,
                                                      distance: PriceValue,
                                                      timeInForce: TimeInForce,
                                                      gtdTime: Option[DateTime],
                                                      triggerCondition: OrderTriggerCondition,
                                                      reason: TrailingStopLossOrderReason,
                                                      clientExtensions: Option[ClientExtensions],
                                                      orderFillTransactionID: TransactionID,
                                                      intendedReplacesOrderID: Option[OrderID],
                                                      rejectReason: TransactionRejectReason
                                                     ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * An OrderFillTransaction represents the filling of an Order in the client’s Account.
      *
      * @param id             The Transaction’s Identifier.
      * @param time           The date/time when the Transaction was created.
      * @param userID         The ID of the user that initiated the creation of the Transaction.
      * @param accountID      The ID of the Account the Transaction was created for.
      * @param batchID        The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID      The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type           The Type of the Transaction. Always set to "ORDER_FILL" for an OrderFillTransaction.
      * @param orderID        The ID of the Order filled.
      * @param clientOrderID  The client Order ID of the Order filled (only provided if the client has assigned one).
      * @param instrument     The name of the filled Order’s instrument.
      * @param units          The number of units filled by the Order.
      * @param price          The average market price that the Order was filled at.
      * @param reason         The reason that an Order was filled
      * @param pl             The profit or loss incurred when the Order was filled.
      * @param financing      The financing paid or collected when the Order was filled.
      * @param accountBalance The Account’s balance after the Order was filled.
      * @param tradeOpened    The Trade that was opened when the Order was filled (only provided if filling the Order resulted in a new Trade).
      * @param tradesClosed   The Trades that were closed when the Order was filled (only provided if filling the Order resulted in a closing open Trades).
      * @param tradeReduced   The Trade that was reduced when the Order was filled (only provided if filling the Order resulted in reducing an open Trade).
      */
    case class OrderFillTransaction(id: TransactionID,
                                    time: DateTime,
                                    userID: Int,
                                    accountID: AccountID,
                                    batchID: TransactionID,
                                    requestID: Option[RequestID],
                                    `type`: TransactionType = TransactionType.ORDER_FILL,
                                    orderID: OrderID,
                                    clientOrderID: ClientID,
                                    instrument: InstrumentName,
                                    units: Double,
                                    price: PriceValue,
                                    reason: OrderFillReason,
                                    pl: AccountUnits,
                                    financing: AccountUnits,
                                    accountBalance: AccountUnits,
                                    tradeOpened: TradeOpen,
                                    tradesClosed: Seq[TradeReduce],
                                    tradeReduced: TradeReduce
                                   ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * An OrderCancelTransaction represents the cancellation of an Order in the client’s Account.
      *
      * @param id                The Transaction’s Identifier.
      * @param time              The date/time when the Transaction was created.
      * @param userID            The ID of the user that initiated the creation of the Transaction.
      * @param accountID         The ID of the Account the Transaction was created for.
      * @param batchID           The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID         The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type              The Type of the Transaction. Always set to "ORDER_CANCEL" for an OrderCancelTransaction.
      * @param orderID           The ID of the Order cancelled
      * @param clientOrderID     The client ID of the Order cancelled (only provided if the Order has a client Order ID).
      * @param reason            The reason that the Order was cancelled.
      * @param replacedByOrderID The ID of the Order that replaced this Order (only provided if this Order was cancelled for replacement).
      */
    case class OrderCancelTransaction(id: TransactionID,
                                      time: DateTime,
                                      userID: Int,
                                      accountID: AccountID,
                                      batchID: TransactionID,
                                      requestID: Option[RequestID],
                                      `type`: TransactionType = TransactionType.ORDER_CANCEL,
                                      orderID: OrderID,
                                      clientOrderID: OrderID,
                                      reason: OrderCancelReason,
                                      replacedByOrderID: Option[OrderID]
                                     ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * An OrderCancelRejectTransaction represents the rejection of the cancellation of an Order in the client’s Account.
      *
      * @param id            The Transaction’s Identifier.
      * @param time          The date/time when the Transaction was created.
      * @param userID        The ID of the user that initiated the creation of the Transaction.
      * @param accountID     The ID of the Account the Transaction was created for.
      * @param batchID       The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID     The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type          The Type of the Transaction. Always set to "ORDER_CANCEL_REJECT" for an OrderCancelRejectTransaction.
      * @param orderID       The ID of the Order intended to be cancelled
      * @param clientOrderID The client ID of the Order intended to be cancelled (only provided if the Order has a client Order ID).
      * @param reason        The reason that the Order was to be cancelled.
      * @param rejectReason  The reason that the Reject Transaction was created
      */
    case class OrderCancelRejectTransaction(id: TransactionID,
                                            time: DateTime,
                                            userID: Int,
                                            accountID: AccountID,
                                            batchID: TransactionID,
                                            requestID: Option[RequestID],
                                            `type`: TransactionType = TransactionType.ORDER_CANCEL_REJECT,
                                            orderID: OrderID,
                                            clientOrderID: OrderID,
                                            reason: OrderCancelReason,
                                            rejectReason: TransactionRejectReason
                                           ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A OrderClientExtensionsModifyTransaction represents the modification of an Order’s Client Extensions.
      *
      * @param id                          The Transaction’s Identifier.
      * @param time                        The date/time when the Transaction was created.
      * @param userID                      The ID of the user that initiated the creation of the Transaction.
      * @param accountID                   The ID of the Account the Transaction was created for.
      * @param batchID                     The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID                   The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                        The Type of the Transaction. Always set to "ORDER_CLIENT_EXTENSIONS_MODIFY" for a OrderClienteExtensionsModifyTransaction.
      * @param orderID                     The ID of the Order who’s client extensions are to be modified.
      * @param clientOrderID               The original Client ID of the Order who’s client extensions are to be modified.
      * @param clientExtensionsModify      The new Client Extensions for the Order.
      * @param tradeClientExtensionsModify The new Client Extensions for the Order’s Trade on fill.
      */
    case class OrderClientExtensionsModifyTransaction(id: TransactionID,
                                                      time: DateTime,
                                                      userID: Int,
                                                      accountID: AccountID,
                                                      batchID: TransactionID,
                                                      requestID: Option[RequestID],
                                                      `type`: TransactionType = TransactionType.ORDER_CLIENT_EXTENSIONS_MODIFY,
                                                      orderID: OrderID,
                                                      clientOrderID: ClientID,
                                                      clientExtensionsModify: ClientExtensions,
                                                      tradeClientExtensionsModify: ClientExtensions
                                                     ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A OrderClientExtensionsModifyRejectTransaction represents the rejection of the modification of an Order’s Client Extensions.
      *
      * @param id                          The Transaction’s Identifier.
      * @param time                        The date/time when the Transaction was created.
      * @param userID                      The ID of the user that initiated the creation of the Transaction.
      * @param accountID                   The ID of the Account the Transaction was created for.
      * @param batchID                     The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID                   The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                        The Type of the Transaction. Always set to "ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT" for a OrderClientExtensionsModifyRejectTransaction.
      * @param orderID                     The ID of the Order who’s client extensions are to be modified.
      * @param clientOrderID               The original Client ID of the Order who’s client extensions are to be modified.
      * @param clientExtensionsModify      The new Client Extensions for the Order.
      * @param tradeClientExtensionsModify The new Client Extensions for the Order’s Trade on fill.
      * @param rejectReason                The reason that the Reject Transaction was created
      */
    case class OrderClientExtensionsModifyRejectTransaction(id: TransactionID,
                                                            time: DateTime,
                                                            userID: Int,
                                                            accountID: AccountID,
                                                            batchID: TransactionID,
                                                            requestID: Option[RequestID],
                                                            `type`: TransactionType = TransactionType.ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT,
                                                            orderID: OrderID,
                                                            clientOrderID: ClientID,
                                                            clientExtensionsModify: ClientExtensions,
                                                            tradeClientExtensionsModify: ClientExtensions,
                                                            rejectReason: TransactionRejectReason
                                                           ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TradeClientExtensionsModifyTransaction represents the modification of a Trade’s Client Extensions.
      *
      * @param id                          The Transaction’s Identifier.
      * @param time                        The date/time when the Transaction was created.
      * @param userID                      The ID of the user that initiated the creation of the Transaction.
      * @param accountID                   The ID of the Account the Transaction was created for.
      * @param batchID                     The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID                   The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                        The Type of the Transaction. Always set to "TRADE_CLIENT_EXTENSIONS_MODIFY" for a TradeClientExtensionsModifyTransaction.
      * @param tradeID                     The ID of the Trade who’s client extensions are to be modified.
      * @param clientTradeID               The original Client ID of the Trade who’s client extensions are to be modified.
      * @param tradeClientExtensionsModify The new Client Extensions for the Trade.
      */
    case class TradeClientExtensionsModifyTransaction(id: TransactionID,
                                                      time: DateTime,
                                                      userID: Int,
                                                      accountID: AccountID,
                                                      batchID: TransactionID,
                                                      requestID: Option[RequestID],
                                                      `type`: TransactionType = TransactionType.TRADE_CLIENT_EXTENSIONS_MODIFY,
                                                      tradeID: TradeID,
                                                      clientTradeID: ClientID,
                                                      tradeClientExtensionsModify: ClientExtensions
                                                     ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A TradeClientExtensionsModifyRejectTransaction represents the rejection of the modification of a Trade’s Client Extensions.
      *
      * @param id                          The Transaction’s Identifier.
      * @param time                        The date/time when the Transaction was created.
      * @param userID                      The ID of the user that initiated the creation of the Transaction.
      * @param accountID                   The ID of the Account the Transaction was created for.
      * @param batchID                     The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID                   The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                        The Type of the Transaction. Always set to "TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT" for a TradeClientExtensionsModifyRejectTransaction.
      * @param tradeID                     The ID of the Trade who’s client extensions are to be modified.
      * @param clientTradeID               The original Client ID of the Trade who’s client extensions are to be modified.
      * @param tradeClientExtensionsModify The new Client Extensions for the Trade.
      * @param rejectReason                The reason that the Reject Transaction was created
      */
    case class TradeClientExtensionsModifyRejectTransaction(id: TransactionID,
                                                            time: DateTime,
                                                            userID: Int,
                                                            accountID: AccountID,
                                                            batchID: TransactionID,
                                                            requestID: Option[RequestID],
                                                            `type`: TransactionType = TransactionType.TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT,
                                                            tradeID: TradeID,
                                                            clientTradeID: ClientID,
                                                            tradeClientExtensionsModify: ClientExtensions,
                                                            rejectReason: TransactionRejectReason
                                                           ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarginCallEnterTransaction is created when an Account enters the margin call state.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "MARGIN_CALL_ENTER" for an MarginCallEnterTransaction.
      */
    case class MarginCallEnterTransaction(id: TransactionID,
                                          time: DateTime,
                                          userID: Int,
                                          accountID: AccountID,
                                          batchID: TransactionID,
                                          requestID: Option[RequestID],
                                          `type`: TransactionType = TransactionType.MARGIN_CALL_ENTER
                                         ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarginCallExtendTransaction is created when the margin call state for an Account has been extended.
      *
      * @param id              The Transaction’s Identifier.
      * @param time            The date/time when the Transaction was created.
      * @param userID          The ID of the user that initiated the creation of the Transaction.
      * @param accountID       The ID of the Account the Transaction was created for.
      * @param batchID         The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID       The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type            The Type of the Transaction. Always set to "MARGIN_CALL_EXTEND" for an MarginCallExtendTransaction.
      * @param extensionNumber The number of the extensions to the Account’s current margin call that have been applied. This value will be set to 1 for the first MarginCallExtend Transaction
      */
    case class MarginCallExtendTransaction(id: TransactionID,
                                           time: DateTime,
                                           userID: Int,
                                           accountID: AccountID,
                                           batchID: TransactionID,
                                           requestID: Option[RequestID],
                                           `type`: TransactionType = TransactionType.MARGIN_CALL_EXTEND,
                                           extensionNumber: Int
                                          ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A MarginCallExitnterTransaction is created when an Account leaves the margin call state.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "MARGIN_CALL_EXIT" for an MarginCallExitTransaction.
      */
    case class MarginCallExitTransaction(id: TransactionID,
                                         time: DateTime,
                                         userID: Int,
                                         accountID: AccountID,
                                         batchID: TransactionID,
                                         requestID: Option[RequestID],
                                         `type`: TransactionType = TransactionType.MARGIN_CALL_EXIT
                                        ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A DelayedTradeClosure Transaction is created administratively to indicate open trades that should have been closed but weren’t because the open trades’ instruments were untradeable at the time. Open trades listed in this transaction will be closed once their respective instruments become tradeable.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "DELAYED_TRADE_CLOSURE" for an DelayedTradeClosureTransaction.
      * @param reason    The reason for the delayed trade closure
      * @param tradeIDs  List of Trade ID’s identifying the open trades that will be closed when their respective instruments become tradeable
      */
    case class DelayedTradeClosureTransaction(id: TransactionID,
                                              time: DateTime,
                                              userID: Int,
                                              accountID: AccountID,
                                              batchID: TransactionID,
                                              requestID: Option[RequestID],
                                              `type`: TransactionType = TransactionType.DELAYED_TRADE_CLOSURE,
                                              reason: MarketOrderReason,
                                              tradeIDs: TradeID
                                             ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A DailyFinancingTransaction represents the daily payment/collection of financing for an Account.
      *
      * @param id                   The Transaction’s Identifier.
      * @param time                 The date/time when the Transaction was created.
      * @param userID               The ID of the user that initiated the creation of the Transaction.
      * @param accountID            The ID of the Account the Transaction was created for.
      * @param batchID              The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID            The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type                 The Type of the Transaction. Always set to "DAILY_FINANCING" for a DailyFinancingTransaction.
      * @param financing            The amount of financing paid/collected for the Account.
      * @param accountBalance       The Account’s balance after daily financing.
      * @param accountFinancingMode The account financing mode at the time of the daily financing.
      * @param positionFinancings   The financing paid/collected for each Position in the Account.
      */
    case class DailyFinancingTransaction(id: TransactionID,
                                         time: DateTime,
                                         userID: Int,
                                         accountID: AccountID,
                                         batchID: TransactionID,
                                         requestID: Option[RequestID],
                                         `type`: TransactionType = TransactionType.DAILY_FINANCING,
                                         financing: AccountUnits,
                                         accountBalance: AccountUnits,
                                         accountFinancingMode: AccountFinancingMode,
                                         positionFinancings: Seq[PositionFinancing]
                                        ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * A ResetResettablePLTransaction represents the resetting of the Account’s resettable PL counters.
      *
      * @param id        The Transaction’s Identifier.
      * @param time      The date/time when the Transaction was created.
      * @param userID    The ID of the user that initiated the creation of the Transaction.
      * @param accountID The ID of the Account the Transaction was created for.
      * @param batchID   The ID of the "batch" that the Transaction belongs to. Transactions in the same batch are applied to the Account simultaneously.
      * @param requestID The Request ID of the Account Control Request which generated the transaction (only provided for Transactions created by a Client request)
      * @param type      The Type of the Transaction. Always set to "RESET_RESETTABLE_PL" for a ResetResettablePLTransaction.
      */
    case class ResetResettablePLTransaction(id: TransactionID,
                                            time: DateTime,
                                            userID: Int,
                                            accountID: AccountID,
                                            batchID: TransactionID,
                                            requestID: Option[RequestID],
                                            `type`: TransactionType = TransactionType.RESET_RESETTABLE_PL
                                           ) extends Transaction(id, time, userID, accountID, batchID, requestID, `type`)

    /**
      * The unique Transaction identifier within each Account.
      * Format: String representation of the numerical OANDA-assigned TransactionID
      * Example: 1523
      */
    case class TransactionID(value: String) extends AnyVal

    /**
      * The possible types of a Transaction
      */
    object TransactionType extends Enumeration {
      type TransactionType = Value

      /** Account Create Transaction */
      val CREATE = Value

      /** Market if Touched Order Reject Transaction */
      val MARKET_IF_TOUCHED_ORDER_REJECT = Value

      /** Order Client Extensions Modify Reject Transaction */
      val ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT = Value

      /** Limit Order Reject Transaction */
      val LIMIT_ORDER_REJECT = Value

      /** Take Profit Order Reject Transaction */
      val TAKE_PROFIT_ORDER_REJECT = Value

      /** Limit Order Transaction */
      val LIMIT_ORDER = Value

      /** Trade Client Extensions Modify Transaction */
      val TRADE_CLIENT_EXTENSIONS_MODIFY = Value

      /** Trade Client Extensions Modify Reject Transaction */
      val TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT = Value

      /** Transfer Funds Reject Transaction */
      val TRANSFER_FUNDS_REJECT = Value

      /** Reset Resettable PL Transaction */
      val RESET_RESETTABLE_PL = Value

      /** Order Cancel Transaction */
      val ORDER_CANCEL = Value

      /** Stop Loss Order Reject Transaction */
      val STOP_LOSS_ORDER_REJECT = Value

      /** Client Configuration Transaction */
      val CLIENT_CONFIGURE = Value

      /** Margin Call Extend Transaction */
      val MARGIN_CALL_EXTEND = Value

      /** Take Profit Order Transaction */
      val TAKE_PROFIT_ORDER = Value

      /** Order Client Extensions Modify Transaction */
      val ORDER_CLIENT_EXTENSIONS_MODIFY = Value

      /** Daily Financing Transaction */
      val DAILY_FINANCING = Value

      /** Order Fill Transaction */
      val ORDER_FILL = Value

      /** Account Close Transaction */
      val CLOSE = Value

      /** Delayed Trade Closure Transaction */
      val DELAYED_TRADE_CLOSURE = Value

      /** Market Order Transaction */
      val MARKET_ORDER = Value

      /** Client Configuration Reject Transaction */
      val CLIENT_CONFIGURE_REJECT = Value

      /** Margin Call Enter Transaction */
      val MARGIN_CALL_ENTER = Value

      /** Order Cancel Reject Transaction */
      val ORDER_CANCEL_REJECT = Value

      /** Market Order Reject Transaction */
      val MARKET_ORDER_REJECT = Value

      /** Margin Call Exit Transaction */
      val MARGIN_CALL_EXIT = Value

      /** Trailing Stop Loss Order Reject Transaction */
      val TRAILING_STOP_LOSS_ORDER_REJECT = Value

      /** Stop Loss Order Transaction */
      val STOP_LOSS_ORDER = Value

      /** Stop Order Reject Transaction */
      val STOP_ORDER_REJECT = Value

      /** Account Reopen Transaction */
      val REOPEN = Value

      /** Market if Touched Order Transaction */
      val MARKET_IF_TOUCHED_ORDER = Value

      /** Transfer Funds Transaction */
      val TRANSFER_FUNDS = Value

      /** Stop Order Transaction */
      val STOP_ORDER = Value

      /** Trailing Stop Loss Order Transaction */
      val TRAILING_STOP_LOSS_ORDER = Value
    }

    /**
      * The reason that an Account is being funded.
      */
    object FundingReason extends Enumeration {
      type FundingReason = Value

      /** Funds are being transfered between two Accounts. */
      val ACCOUNT_TRANSFER = Value

      /** The client has initiated a funds transfer */
      val CLIENT_FUNDING = Value

      /** Funds are being transfered as part of a Site migration */
      val SITE_MIGRATION = Value

      /** Funds are being transfered as part of a Division migration */
      val DIVISION_MIGRATION = Value

      /** Funds are being transfered as part of an Account adjustment */
      val ADJUSTMENT = Value
    }

    /**
      * The reason that the Market Order was created
      */
    object MarketOrderReason extends Enumeration {
      type MarketOrderReason = Value

      /** The Market Order was created to close a Position at the request of a client */
      val POSITION_CLOSEOUT = Value

      /** The Market Order was created as part of a Margin Closeout */
      val MARGIN_CLOSEOUT = Value

      /** The Market Order was created to close a trade marked for delayed closure */
      val DELAYED_TRADE_CLOSE = Value

      /** The Market Order was created at the request of a client */
      val CLIENT_ORDER = Value

      /** The Market Order was created to close a Trade at the request of a client */
      val TRADE_CLOSE = Value
    }

    /**
      * The reason that the Limit Order was initiated
      */
    object LimitOrderReason extends Enumeration {
      type LimitOrderReason = Value

      /** The Limit Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Limit Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value
    }

    /**
      * The reason that the Stop Order was initiated
      */
    object StopOrderReason extends Enumeration {
      type StopOrderReason = Value

      /** The Stop Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Stop Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value
    }

    /**
      * The reason that the Market-if-touched Order was initiated
      */
    object MarketIfTouchedOrderReason extends Enumeration {
      type MarketIfTouchedOrderReason = Value

      /** The Market-if-touched Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Market-if-touched Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value
    }

    /**
      * The reason that the Take Profit Order was initiated
      */
    object TakeProfitOrderReason extends Enumeration {
      type TakeProfitOrderReason = Value

      /** The Take Profit Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Take Profit Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value

      /** The Take Profit Order was initiated automatically when an Order was filled that opened a new Trade requiring a Take Profit Order. */
      val ON_FILL = Value
    }

    /**
      * The reason that the Stop Loss Order was initiated
      */
    object StopLossOrderReason extends Enumeration {
      type StopLossOrderReason = Value

      /** The Stop Loss Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Stop Loss Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value

      /** The Stop Loss Order was initiated automatically when an Order was filled that opened a new Trade requiring a Stop Loss Order. */
      val ON_FILL = Value
    }

    /**
      * The reason that the Trailing Stop Loss Order was initiated
      */
    object TrailingStopLossOrderReason extends Enumeration {
      type TrailingStopLossOrderReason = Value

      /** The Trailing Stop Loss Order was initiated at the request of a client */
      val CLIENT_ORDER = Value

      /** The Trailing Stop Loss Order was initiated as a replacement for an existing Order */
      val REPLACEMENT = Value

      /** The Trailing Stop Loss Order was initiated automatically when an Order was filled that opened a new Trade requiring a Trailing Stop Loss Order. */
      val ON_FILL = Value
    }

    /**
      * The reason that an Order was filled
      */
    object OrderFillReason extends Enumeration {
      type OrderFillReason = Value

      /** The Order filled was a Limit Order */
      val LIMIT_ORDER = Value

      /** The Order filled was a Market Order used to explicitly close a Position */
      val MARKET_ORDER_POSITION_CLOSEOUT = Value

      /** The Order filled was a Take Profit Order */
      val TAKE_PROFIT_ORDER = Value

      /** The Order filled was a Market Order used for a delayed Trade close */
      val MARKET_ORDER_DELAYED_TRADE_CLOSE = Value

      /** The Order filled was a Market Order */
      val MARKET_ORDER = Value

      /** The Order filled was a Market Order used to explicitly close a Trade */
      val MARKET_ORDER_TRADE_CLOSE = Value

      /** The Order filled was a Market Order used for a Margin Closeout */
      val MARKET_ORDER_MARGIN_CLOSEOUT = Value

      /** The Order filled was a Stop Loss Order */
      val STOP_LOSS_ORDER = Value

      /** The Order filled was a Market-if-touched Order */
      val MARKET_IF_TOUCHED_ORDER = Value

      /** The Order filled was a Stop Order */
      val STOP_ORDER = Value

      /** The Order filled was a Trailing Stop Loss Order */
      val TRAILING_STOP_LOSS_ORDER = Value
    }

    /**
      * The reason that an Order was cancelled.
      */
    object OrderCancelReason extends Enumeration {
      type OrderCancelReason = Value

      /** Filling the Order wasn’t possible because the Account had insufficient margin. */
      val INSUFFICIENT_MARGIN = Value

      /** Filling the Order would have violated the Order’s price bound. */
      val BOUNDS_VIOLATION = Value

      /** Filling the Order would have resulted in the creation of a Stop Loss Order with a GTD time in the past. */
      val STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** Filling the Order would have resulted in the creation of a Trailing Stop Loss Order with a GTD time in the past. */
      val TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** The Order was cancelled explicitly at the request of the client. */
      val CLIENT_REQUEST = Value

      /** Filling the Order would have resulted in the creation of a Take Profit Order with a client Order ID that is already in use. */
      val TAKE_PROFIT_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS = Value

      /** Filling the Order wasn’t possible because enough liquidity available. */
      val INSUFFICIENT_LIQUIDITY = Value

      /** Filling the Order would have resulted in exceeding the number of pending Orders allowed for the Account. */
      val PENDING_ORDERS_ALLOWED_EXCEEDED = Value

      /** Filling the Order would have resulted in a a FIFO violation. */
      val FIFO_VIOLATION = Value

      /** Filling the Order would have resulted in the creation of a Stop Loss Order with a client Order ID that is already in use. */
      val STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS = Value

      /** Filling the Order would result in the creation of a new Open Trade with a client Trade ID already in use. */
      val CLIENT_TRADE_ID_ALREADY_EXISTS = Value

      /** The Order was cancelled because at the time of filling the account was locked. */
      val ACCOUNT_LOCKED = Value

      /** Filling the Order would result in the creation of a Stop Loss Order that would have been filled immediately, closing the new Trade at a loss. */
      val STOP_LOSS_ON_FILL_LOSS = Value

      /** Filling the Order would have resulted in the Account’s maximum position size limit being exceeded for the Order’s instrument. */
      val POSITION_SIZE_EXCEEDED = Value

      /** The Order is linked to an open Trade that was closed. */
      val LINKED_TRADE_CLOSED = Value

      /** Filling the Order would cause the maximum open trades allowed for the Account to be exceeded. */
      val OPEN_TRADES_ALLOWED_EXCEEDED = Value

      /** Filling the Order would result in the creation of a Take Profit Order that would have been filled immediately, closing the new Trade at a loss. */
      val TAKE_PROFIT_ON_FILL_LOSS = Value

      /** Filling the Order was not possible because the Account is locked for filling Orders. */
      val ACCOUNT_ORDER_FILL_LOCKED = Value

      /** The Order cancelled because it is being migrated to another account. */
      val MIGRATION = Value

      /** Filling the Order would have resulted in the creation of a Trailing Stop Loss Order with a client Order ID that is already in use. */
      val TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS = Value

      /** The time in force specified for this order has passed. */
      val TIME_IN_FORCE_EXPIRED = Value

      /** The Order was cancelled because at the time of filling, an unexpected internal server error occurred. */
      val INTERNAL_SERVER_ERROR = Value

      /** The order was to be filled, however the account is configured to not allow new positions to be created. */
      val ACCOUNT_NEW_POSITIONS_LOCKED = Value

      /** Filling the Order wasn’t possible because it required the creation of a dependent Order and the Account is locked for Order creation. */
      val ACCOUNT_ORDER_CREATION_LOCKED = Value

      /** The Order was cancelled for replacement at the request of the client. */
      val CLIENT_REQUEST_REPLACED = Value

      /** Closing out a position wasn’t fully possible. */
      val POSITION_CLOSEOUT_FAILED = Value

      /** Filling the Order wasn’t possible because the Order’s instrument was halted. */
      val MARKET_HALTED = Value

      /** Filling the Order would have resulted in the creation of a Take Profit Order with a GTD time in the past. */
      val TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** Filling the Order would result in the creation of a Take Profit Loss Order that would close the new Trade at a loss when filled. */
      val LOSING_TAKE_PROFIT = Value
    }

    /**
      * A client-provided identifier, used by clients to refer to their Orders or Trades with an identifier that they have provided.
      * Example: my_order_id
      */
    case class ClientID(value: String) extends AnyVal

    /**
      * A client-provided tag that can contain any data and may be assigned to their Orders or Trades. Tags are typically used to associate groups of Trades and/or Orders together.
      * Example: client_tag_1
      */
    case class ClientTag(value: String) extends AnyVal

    /**
      * A client-provided comment that can contain any data and may be assigned to their Orders or Trades. Comments are typically used to provide extra context or meaning to an Order or Trade.
      * Example: This is a client comment
      */
    case class ClientComment(value: String) extends AnyVal

    /**
      * A ClientExtensions object allows a client to attach a clientID, tag and comment to Orders and Trades in their Account. Do not set, modify, or delete this field if your account is associated with MT4.
      *
      * @param id      The Client ID of the Order/Trade
      * @param tag     A tag associated with the Order/Trade
      * @param comment A comment associated with the Order/Trade
      */
    case class ClientExtensions(id: ClientID,
                                tag: ClientTag,
                                comment: Option[ClientComment])

    /**
      * TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      *
      * @param price            The price that the Take Profit Order will be triggered at.
      * @param timeInForce      The time in force for the created Take Profit Order. This may only be GTC, GTD or GFD.
      * @param gtdTime          The date when the Take Profit Order will be cancelled on if timeInForce is GTD.
      * @param clientExtensions The Client Extensions to add to the Take Profit Order when created.
      */
    case class TakeProfitDetails(price: PriceValue,
                                 timeInForce: TimeInForce,
                                 gtdTime: Option[DateTime] = None,
                                 clientExtensions: Option[ClientExtensions] = None)

    /**
      * StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      *
      * @param price            The price that the Stop Loss Order will be triggered at.
      * @param timeInForce      The time in force for the created Stop Loss Order. This may only be GTC, GTD or GFD.
      * @param gtdTime          The date when the Stop Loss Order will be cancelled on if timeInForce is GTD.
      * @param clientExtensions The Client Extensions to add to the Stop Loss Order when created.
      */
    case class StopLossDetails(price: PriceValue,
                               timeInForce: TimeInForce,
                               gtdTime: Option[DateTime] = None,
                               clientExtensions: Option[ClientExtensions] = None)

    /**
      * TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      *
      * @param distance         The distance (in price units) from the Trade’s fill price that the Trailing Stop Loss Order will be triggered at.
      * @param timeInForce      The time in force for the created Trailing Stop Loss Order. This may only be GTC, GTD or GFD.
      * @param gtdTime          The date when the Trailing Stop Loss Order will be cancelled on if timeInForce is GTD.
      * @param clientExtensions The Client Extensions to add to the Trailing Stop Loss Order when created.
      */
    case class TrailingStopLossDetails(distance: PriceValue,
                                       timeInForce: TimeInForce,
                                       gtdTime: Option[DateTime] = None,
                                       clientExtensions: Option[ClientExtensions] = None)

    /**
      * A TradeOpen object represents a Trade for an instrument that was opened in an Account. It is found embedded in Transactions that affect the position of an instrument in the Account, specifically the OrderFill Transaction.
      *
      * @param tradeID          The ID of the Trade that was opened
      * @param units            The number of units opened by the Trade
      * @param clientExtensions The client extensions for the newly opened Trade
      */
    case class TradeOpen(tradeID: TradeID,
                         units: Double,
                         clientExtensions: Option[ClientExtensions])

    /**
      * A TradeReduce object represents a Trade for an instrument that was reduced (either partially or fully) in an Account. It is found embedded in Transactions that affect the position of an instrument in the account, specifically the OrderFill Transaction.
      *
      * @param tradeID    The ID of the Trade that was reduced or closed
      * @param units      The number of units that the Trade was reduced by
      * @param realizedPL The PL realized when reducing the Trade
      * @param financing  The financing paid/collected when reducing the Trade
      */
    case class TradeReduce(tradeID: TradeID,
                           units: Double,
                           realizedPL: AccountUnits,
                           financing: AccountUnits)

    /**
      * A MarketOrderTradeClose specifies the extensions to a Market Order that has been created specifically to close a Trade.
      *
      * @param tradeID       The ID of the Trade requested to be closed
      * @param clientTradeID The client ID of the Trade requested to be closed
      * @param units         Indication of how much of the Trade to close. Either "ALL", or a DecimalNumber reflection a partial close of the Trade.
      */
    case class MarketOrderTradeClose(tradeID: TradeID,
                                     clientTradeID: String,
                                     units: String)

    /**
      * Details for the Market Order extensions specific to a Market Order placed that is part of a Market Order Margin Closeout in a client’s account
      *
      * @param reason The reason the Market Order was created to perform a margin closeout
      */
    case class MarketOrderMarginCloseout(reason: MarketOrderMarginCloseoutReason)

    /**
      * The reason that the Market Order was created to perform a margin closeout
      */
    object MarketOrderMarginCloseoutReason extends Enumeration {
      type MarketOrderMarginCloseoutReason = Value

      /** Trade closures resulted from violating OANDA’s margin policy */
      val MARGIN_CHECK_VIOLATION = Value

      /** Trade closures came from a margin closeout event resulting from regulatory conditions placed on the Account’s margin call */
      val REGULATORY_MARGIN_CALL_VIOLATION = Value
    }

    /**
      * Details for the Market Order extensions specific to a Market Order placed with the intent of fully closing a specific open trade that should have already been closed but wasn’t due to halted market conditions
      *
      * @param tradeID             The ID of the Trade being closed
      * @param clientTradeID       The Client ID of the Trade being closed
      * @param sourceTransactionID The Transaction ID of the DelayedTradeClosure transaction to which this Delayed Trade Close belongs to
      */
    case class MarketOrderDelayedTradeClose(tradeID: TradeID,
                                            clientTradeID: TradeID,
                                            sourceTransactionID: TransactionID)

    /**
      * A MarketOrderPositionCloseout specifies the extensions to a Market Order when it has been created to closeout a specific Position.
      *
      * @param instrument The instrument of the Position being closed out.
      * @param units      Indication of how much of the Position to close. Either "ALL", or a DecimalNumber reflection a partial close of the Trade. The DecimalNumber must always be positive, and represent a number that doesn’t exceed the absolute size of the Position.
      */
    case class MarketOrderPositionCloseout(instrument: InstrumentName,
                                           units: String)

    /**
      * A VWAP Receipt provides a record of how the price for an Order fill is constructed. If the Order is filled with multiple buckets in a depth of market, each bucket will be represented with a VWAP Receipt.
      *
      * @param units The number of units filled
      * @param price The price at which the units were filled
      */
    case class VWAPReceipt(units: Double,
                           price: PriceValue
                          )

    /**
      * A LiquidityRegenerationSchedule indicates how liquidity that is used when filling an Order for an instrument is regenerated following the fill. A liquidity regeneration schedule will be in effect until the timestamp of its final step, but may be replaced by a schedule created for an Order of the same instrument that is filled while it is still in effect.
      *
      * @param steps The steps in the Liquidity Regeneration Schedule
      */
    case class LiquidityRegenerationSchedule(steps: Seq[LiquidityRegenerationScheduleStep])

    /**
      * A liquidity regeneration schedule Step indicates the amount of bid and ask liquidity that is used by the Account at a certain time. These amounts will only change at the timestamp of the following step.
      *
      * @param timestamp        The timestamp of the schedule step.
      * @param bidLiquidityUsed The amount of bid liquidity used at this step in the schedule.
      * @param askLiquidityUsed The amount of ask liquidity used at this step in the schedule.
      */
    case class LiquidityRegenerationScheduleStep(timestamp: DateTime,
                                                 bidLiquidityUsed: Double,
                                                 askLiquidityUsed: Double)

    /**
      * OpenTradeFinancing is used to pay/collect daily financing charge for an open Trade within an Account
      *
      * @param tradeID   The ID of the Trade that financing is being paid/collected for.
      * @param financing The amount of financing paid/collected for the Trade.
      */
    case class OpenTradeFinancing(tradeID: TradeID,
                                  financing: AccountUnits)

    /**
      * OpenTradeFinancing is used to pay/collect daily financing charge for a Position within an Account
      *
      * @param instrumentID        The instrument of the Position that financing is being paid/collected for.
      * @param financing           The amount of financing paid/collected for the Position.
      * @param openTradeFinancings The financing paid/collecte for each open Trade within the Position.
      */
    case class PositionFinancing(instrumentID: Option[InstrumentName],
                                 financing: AccountUnits,
                                 openTradeFinancings: Seq[OpenTradeFinancing])

    /**
      * The request identifier. Used by administrators to refer to a client’s request.
      */
    case class RequestID(value: String) extends AnyVal

    /**
      * The reason that a Transaction was rejected.
      */
    object TransactionRejectReason extends Enumeration {
      type TransactionRejectReason = Value

      /** The Take Profit on fill specified contains a price with more precision than is allowed by the Order’s instrument */
      val TAKE_PROFIT_ON_FILL_PRICE_PRECISION_EXCEEDED = Value

      /** Multiple Orders on fill share the same client Order ID */
      val ORDERS_ON_FILL_DUPLICATE_CLIENT_ORDER_IDS = Value

      /** The OrderFillPositionAction field has not been specified */
      val ORDER_PARTIAL_FILL_OPTION_MISSING = Value

      /** The Take Profit on fill specifies a GTD TimeInForce but does not provide a GTD timestamp */
      val TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_MISSING = Value

      /** The Position requested to be closed out does not exist */
      val CLOSEOUT_POSITION_DOESNT_EXIST = Value

      /** The client Order ID specified is invalid */
      val CLIENT_ORDER_ID_INVALID = Value

      /** The markup group ID provided is invalid */
      val MARKUP_GROUP_ID_INVALID = Value

      /** The Stop Loss on fill specifies an invalid price */
      val STOP_LOSS_ON_FILL_PRICE_INVALID = Value

      /** The Take Profit on fill specifies an invalid TimeInForce */
      val TAKE_PROFIT_ON_FILL_TIME_IN_FORCE_INVALID = Value

      /** The TimeInForce is GTD but no GTD timestamp is provided */
      val TIME_IN_FORCE_GTD_TIMESTAMP_MISSING = Value

      /** The price has not been specified */
      val PRICE_MISSING = Value

      /** The Stop Loss on fill specifies a GTD timestamp that is in the past */
      val STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** The Take Profit on fill client Order ID specified is invalid */
      val TAKE_PROFIT_ON_FILL_CLIENT_ORDER_ID_INVALID = Value

      /** The price distance does not meet the minimum allowed amount */
      val PRICE_DISTANCE_MINIMUM_NOT_MET = Value

      /** Order units have not been not specified */
      val UNITS_MISSING = Value

      /** A Take Profit Order for the specified Trade already exists */
      val TAKE_PROFIT_ORDER_ALREADY_EXISTS = Value

      /** The Trailing Stop Loss on fill GTD timestamp is in the past */
      val TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** A client attempted to create either a Trailing Stop Loss order or an order with a Trailing Stop Loss On Fill specified, which may not yet be supported. */
      val TRAILING_STOP_LOSS_ORDERS_NOT_SUPPORTED = Value

      /** Order units specified are invalid */
      val UNITS_INVALID = Value

      /** The client Trade ID specified is invalid */
      val CLIENT_TRADE_ID_INVALID = Value

      /** The request to closeout a Position could not be fully satisfied */
      val CLOSEOUT_POSITION_REJECT = Value

      /** No configuration parameters provided */
      val CLIENT_CONFIGURE_DATA_MISSING = Value

      /** The Account is locked for Order cancellation */
      val ACCOUNT_ORDER_CANCEL_LOCKED = Value

      /** Neither the Order ID nor client Order ID are specified */
      val ORDER_ID_UNSPECIFIED = Value

      /** The request to close a Trade partially did not specify the number of units to close */
      val CLOSE_TRADE_PARTIAL_UNITS_MISSING = Value

      /** The Account is not active */
      val ACCOUNT_NOT_ACTIVE = Value

      /** The Take Profit on fill specified does not provide a TimeInForce */
      val TAKE_PROFIT_ON_FILL_TIME_IN_FORCE_MISSING = Value

      /** The Stop Loss on fill client Order tag specified is invalid */
      val STOP_LOSS_ON_FILL_CLIENT_ORDER_TAG_INVALID = Value

      /** Creating the Order would result in the maximum number of allowed pending Orders being exceeded */
      val PENDING_ORDERS_ALLOWED_EXCEEDED = Value

      /** The margin rate provided is invalid */
      val MARGIN_RATE_INVALID = Value

      /** The TriggerCondition field has not been specified */
      val TRIGGER_CONDITION_MISSING = Value

      /** The Take Profit on fill specified contains an invalid price */
      val TAKE_PROFIT_ON_FILL_PRICE_INVALID = Value

      /** The Trade ID and client Trade ID specified do not identify the same Trade */
      val TRADE_IDENTIFIER_INCONSISTENCY = Value

      /** The Stop Loss on fill specified does not provide a TimeInForce */
      val STOP_LOSS_ON_FILL_TIME_IN_FORCE_MISSING = Value

      /** The PositionAggregationMode provided is not supported/valid. */
      val POSITION_AGGREGATION_MODE_INVALID = Value

      /** The Account is locked for withdrawals */
      val ACCOUNT_WITHDRAWAL_LOCKED = Value

      /** The Trailing Stop Loss on fill price distance does not meet the minimum allowed amount */
      val TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MINIMUM_NOT_MET = Value

      /** The Stop Loss on fill specified does not provide a TriggerCondition */
      val STOP_LOSS_ON_FILL_TRIGGER_CONDITION_MISSING = Value

      /** The Trailing Stop Loss on fill client Order tag specified is invalid */
      val TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_TAG_INVALID = Value

      /** The Trailing Stop Loss on fill client Order ID specified is invalid */
      val TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_INVALID = Value

      /** The Stop Loss on fill specifies an invalid TriggerCondition */
      val STOP_LOSS_ON_FILL_TRIGGER_CONDITION_INVALID = Value

      /** The Trailing Stop Loss on fill TimeInForce is specified as GTD but no GTD timestamp is provided */
      val TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_MISSING = Value

      /** The OrderFillPositionAction specified is invalid */
      val ORDER_FILL_POSITION_ACTION_INVALID = Value

      /** The client Trade ID specifed is already assigned to another open Trade */
      val CLIENT_TRADE_ID_ALREADY_EXISTS = Value

      /** The Account is locked */
      val ACCOUNT_LOCKED = Value

      /** The OrderFillPositionAction field has not been specified */
      val ORDER_FILL_POSITION_ACTION_MISSING = Value

      /** The Order ID and client Order ID specified do not identify the same Order */
      val ORDER_IDENTIFIER_INCONSISTENCY = Value

      /** The Tailing Stop Loss on fill specifies an invalid TriggerCondition */
      val TRAILING_STOP_LOSS_ON_FILL_TRIGGER_CONDITION_INVALID = Value

      /** The request to partially closeout a Position did not specify the number of units to close. */
      val CLOSEOUT_POSITION_PARTIAL_UNITS_MISSING = Value

      /** The price distance exceeds that maximum allowed amount */
      val PRICE_DISTANCE_MAXIMUM_EXCEEDED = Value

      /** The instrument specified is unknown */
      val INSTRUMENT_UNKNOWN = Value

      /** The TimeInForce is GTD but the GTD timestamp is in the past */
      val TIME_IN_FORCE_GTD_TIMESTAMP_IN_PAST = Value

      /** The Trade specified does not exist */
      val TRADE_DOESNT_EXIST = Value

      /** The units specified exceeds the maximum number of units allowed */
      val UNITS_LIMIT_EXCEEDED = Value

      /** Funding amount has not been specified */
      val AMOUNT_MISSING = Value

      /** The request to close a Trade does not specify a full or partial close */
      val CLOSE_TRADE_TYPE_MISSING = Value

      /** The Trailing Stop Loss on fill price distance exceeds the maximum allowed amount */
      val TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MAXIMUM_EXCEEDED = Value

      /** Neither Order nor Trade on Fill client extensions were provided for modification */
      val CLIENT_EXTENSIONS_DATA_MISSING = Value

      /** The Account is locked for deposits */
      val ACCOUNT_DEPOSIT_LOCKED = Value

      /** The Trailing Stop Loss on fill specified does not provide a distance */
      val TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MISSING = Value

      /** A Stop Loss Order for the specified Trade already exists */
      val STOP_LOSS_ORDER_ALREADY_EXISTS = Value

      /** The Take Profit on fill specified does not provide a price */
      val TAKE_PROFIT_ON_FILL_PRICE_MISSING = Value

      /** The Stop Loss on fill specifies an invalid TimeInForce */
      val STOP_LOSS_ON_FILL_TIME_IN_FORCE_INVALID = Value

      /** The Account is locked for configuration */
      val ACCOUNT_CONFIGURATION_LOCKED = Value

      /** The units specified is less than the minimum number of units required */
      val UNITS_MIMIMUM_NOT_MET = Value

      /** The Stop Loss on fill specified does not provide a price */
      val STOP_LOSS_ON_FILL_PRICE_MISSING = Value

      /** The Take Profit on fill specifies an invalid TriggerCondition */
      val TAKE_PROFIT_ON_FILL_TRIGGER_CONDITION_INVALID = Value

      /** The Order to be replaced has a different type than the replacing Order. */
      val REPLACING_ORDER_INVALID = Value

      /** The Account does not have sufficient balance to complete the funding request */
      val INSUFFICIENT_FUNDS = Value

      /** Funding reason has not been specified */
      val FUNDING_REASON_MISSING = Value

      /** The Trailing Stop Loss on fill specified does not provide a TimeInForce */
      val TRAILING_STOP_LOSS_ON_FILL_TIME_IN_FORCE_MISSING = Value

      /** The client Order ID specified is already assigned to another pending Order */
      val CLIENT_ORDER_ID_ALREADY_EXISTS = Value

      /** The system was unable to determine the current price for the Order’s instrument */
      val INSTRUMENT_PRICE_UNKNOWN = Value

      /** The price bound specified is invalid */
      val PRICE_BOUND_INVALID = Value

      /** The Take Profit on fill specified does not provide a TriggerCondition */
      val TAKE_PROFIT_ON_FILL_TRIGGER_CONDITION_MISSING = Value

      /** The TriggerCondition specified is invalid */
      val TRIGGER_CONDITION_INVALID = Value

      /** The Trailing Stop Loss on fill specified does not provide a TriggerCondition */
      val TRAILING_STOP_LOSS_ON_FILL_TRIGGER_CONDITION_MISSING = Value

      /** The Trailing Stop Loss on fill distance contains more precision than is allowed by the instrument */
      val TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_PRECISION_EXCEEDED = Value

      /** A Trailing Stop Loss Order for the specified Trade already exists */
      val TRAILING_STOP_LOSS_ORDER_ALREADY_EXISTS = Value

      /** The price distance specified contains more precision than is allowed for the instrument */
      val PRICE_DISTANCE_PRECISION_EXCEEDED = Value

      /** The Order does not support Trade on fill client extensions because it cannot create a new Trade */
      val TRADE_ON_FILL_CLIENT_EXTENSIONS_NOT_SUPPORTED = Value

      /** The Stop Loss on fill client Order comment specified is invalid */
      val STOP_LOSS_ON_FILL_CLIENT_ORDER_COMMENT_INVALID = Value

      /** The Stop Loss on fill specifies a price with more precision than is allowed by the Order’s instrument */
      val STOP_LOSS_ON_FILL_PRICE_PRECISION_EXCEEDED = Value

      /** The TimeInForce field has not been specified */
      val TIME_IN_FORCE_MISSING = Value

      /** The price specified contains more precision than is allowed for the instrument */
      val PRICE_PRECISION_EXCEEDED = Value

      /** The Trailing Stop Loss on fill client Order comment specified is invalid */
      val TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_COMMENT_INVALID = Value

      /** The price specifed is invalid */
      val PRICE_INVALID = Value

      /** The price distance specifed is invalid */
      val PRICE_DISTANCE_INVALID = Value

      /** The units specified contain more precision than is allowed for the Order’s instrument */
      val UNITS_PRECISION_EXCEEDED = Value

      /** The OrderFillPositionAction specified is invalid. */
      val ORDER_PARTIAL_FILL_OPTION_INVALID = Value

      /** An unexpected internal server error has occurred */
      val INTERNAL_SERVER_ERROR = Value

      /** The Take Profit on fill client Order tag specified is invalid */
      val TAKE_PROFIT_ON_FILL_CLIENT_ORDER_TAG_INVALID = Value

      /** When attempting to reissue an order (currently only a MarketIfTouched) that was immediately partially filled, it is not possible to create a correct pending Order. */
      val INVALID_REISSUE_IMMEDIATE_PARTIAL_FILL = Value

      /** The Stop Loss on fill client Order ID specified is invalid */
      val STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_INVALID = Value

      /** The client Order tag specified is invalid */
      val CLIENT_ORDER_TAG_INVALID = Value

      /** The request to closeout a Position was specified incompletely */
      val CLOSEOUT_POSITION_INCOMPLETE_SPECIFICATION = Value

      /** The replacing Order refers to a different Trade than the Order that is being replaced. */
      val REPLACING_TRADE_ID_INVALID = Value

      /** The Account is locked for Order creation */
      val ACCOUNT_ORDER_CREATION_LOCKED = Value

      /** The price distance has not been specified */
      val PRICE_DISTANCE_MISSING = Value

      /** Funding is not possible because the requested transfer amount is invalid */
      val AMOUNT_INVALID = Value

      /** The Stop Loss on fill specifies a GTD TimeInForce but does not provide a GTD timestamp */
      val STOP_LOSS_ON_FILL_GTD_TIMESTAMP_MISSING = Value

      /** Order instrument has not been specified */
      val INSTRUMENT_MISSING = Value

      /** The instrument specified is not tradeable by the Account */
      val INSTRUMENT_NOT_TRADEABLE = Value

      /** No configuration parameters provided */
      val ADMIN_CONFIGURE_DATA_MISSING = Value

      /** The Order specified does not exist */
      val ORDER_DOESNT_EXIST = Value

      /** The margin rate provided would cause the Account to enter a margin call state. */
      val MARGIN_RATE_WOULD_TRIGGER_MARGIN_CALL = Value

      /** The price bound specified contains more precision than is allowed for the Order’s instrument */
      val PRICE_BOUND_PRECISION_EXCEEDED = Value

      /** A partial Position closeout request specifies a number of units that exceeds the current Position */
      val CLOSEOUT_POSITION_UNITS_EXCEED_POSITION_SIZE = Value

      /** The client Trade comment is invalid */
      val CLIENT_TRADE_COMMENT_INVALID = Value

      /** The request to partially close a Trade specifies a number of units that exceeds the current size of the given Trade */
      val CLOSE_TRADE_UNITS_EXCEED_TRADE_SIZE = Value

      /** The Trailing Stop Loss on fill specifies an invalid TimeInForce */
      val TRAILING_STOP_LOSS_ON_FILL_TIME_IN_FORCE_INVALID = Value

      /** The Trailing Stop Loss on fill distance is invalid */
      val TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_INVALID = Value

      /** The account alias string provided is invalid */
      val ALIAS_INVALID = Value

      /** The Take Profit on fill client Order comment specified is invalid */
      val TAKE_PROFIT_ON_FILL_CLIENT_ORDER_COMMENT_INVALID = Value

      /** The client Trade tag specified is invalid */
      val CLIENT_TRADE_TAG_INVALID = Value

      /** The margin rate provided would cause an immediate margin closeout */
      val MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT = Value

      /** The Take Profit on fill specifies a GTD timestamp that is in the past */
      val TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_IN_PAST = Value

      /** The TimeInForce specified is invalid */
      val TIME_IN_FORCE_INVALID = Value

      /** Neither the Trade ID nor client Trade ID are specified */
      val TRADE_ID_UNSPECIFIED = Value

      /** The client Order comment specified is invalid */
      val CLIENT_ORDER_COMMENT_INVALID = Value
    }

    /**
      * A filter that can be used when fetching Transactions
      */
    object TransactionFilter extends Enumeration {
      type TransactionFilter = Value

      /** Account Create Transaction */
      val CREATE = Value

      /** Market if Touched Order Reject Transaction */
      val MARKET_IF_TOUCHED_ORDER_REJECT = Value

      /** Order Client Extensions Modify Reject Transaction */
      val ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT = Value

      /** Limit Order Reject Transaction */
      val LIMIT_ORDER_REJECT = Value

      /** Take Profit Order Reject Transaction */
      val TAKE_PROFIT_ORDER_REJECT = Value

      /** Limit Order Transaction */
      val LIMIT_ORDER = Value

      /** Trade Client Extensions Modify Transaction */
      val TRADE_CLIENT_EXTENSIONS_MODIFY = Value

      /** Trade Client Extensions Modify Reject Transaction */
      val TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT = Value

      /** Administrative Transactions */
      val ADMIN = Value

      /** Transfer Funds Reject Transaction */
      val TRANSFER_FUNDS_REJECT = Value

      /** Reset Resettable PL Transaction */
      val RESET_RESETTABLE_PL = Value

      /** Order Cancel Transaction */
      val ORDER_CANCEL = Value

      /** Funding-related Transactions */
      val FUNDING = Value

      /** Stop Loss Order Reject Transaction */
      val STOP_LOSS_ORDER_REJECT = Value

      /** Client Configuration Transaction */
      val CLIENT_CONFIGURE = Value

      /** One Cancels All Order Trigger Transaction */
      val ONE_CANCELS_ALL_ORDER_TRIGGERED = Value

      /** Margin Call Extend Transaction */
      val MARGIN_CALL_EXTEND = Value

      /** Take Profit Order Transaction */
      val TAKE_PROFIT_ORDER = Value

      /** Order Client Extensions Modify Transaction */
      val ORDER_CLIENT_EXTENSIONS_MODIFY = Value

      /** Daily Financing Transaction */
      val DAILY_FINANCING = Value

      /** Order Fill Transaction */
      val ORDER_FILL = Value

      /** Account Close Transaction */
      val CLOSE = Value

      /** Delayed Trade Closure Transaction */
      val DELAYED_TRADE_CLOSURE = Value

      /** Market Order Transaction */
      val MARKET_ORDER = Value

      /** Client Configuration Reject Transaction */
      val CLIENT_CONFIGURE_REJECT = Value

      /** Margin Call Enter Transaction */
      val MARGIN_CALL_ENTER = Value

      /** One Cancels All Order Transaction */
      val ONE_CANCELS_ALL_ORDER = Value

      /** Order Cancel Reject Transaction */
      val ORDER_CANCEL_REJECT = Value

      /** Market Order Reject Transaction */
      val MARKET_ORDER_REJECT = Value

      /** Margin Call Exit Transaction */
      val MARGIN_CALL_EXIT = Value

      /** Trailing Stop Loss Order Reject Transaction */
      val TRAILING_STOP_LOSS_ORDER_REJECT = Value

      /** Stop Loss Order Transaction */
      val STOP_LOSS_ORDER = Value

      /** Stop Order Reject Transaction */
      val STOP_ORDER_REJECT = Value

      /** One Cancels All Order Reject Transaction */
      val ONE_CANCELS_ALL_ORDER_REJECT = Value

      /** Account Reopen Transaction */
      val REOPEN = Value

      /** Market if Touched Order Transaction */
      val MARKET_IF_TOUCHED_ORDER = Value

      /** Transfer Funds Transaction */
      val TRANSFER_FUNDS = Value

      /** Order-related Transactions. These are the Transactions that create, cancel, fill or trigger Orders */
      val ORDER = Value

      /** Stop Order Transaction */
      val STOP_ORDER = Value

      /** Trailing Stop Loss Order Transaction */
      val TRAILING_STOP_LOSS_ORDER = Value
    }

    /**
      * A TransactionHeartbeat object is injected into the Transaction stream to ensure that the HTTP connection remains active.
      *
      * @param type              The string "HEARTBEAT"
      * @param lastTransactionID The ID of the most recent Transaction created for the Account
      * @param time              The date/time when the TransactionHeartbeat was created.
      */
    case class TransactionHeartbeat(`type`: String = "HEARTBEAT",
                                    lastTransactionID: TransactionID,
                                    time: DateTime)

  }

  object AccountModel {

    /**
      * The string representation of an Account Identifier.
      * Format: "-"-delimited string with format "{siteID}-{divisionID}-{userID}-{accountNumber}"
      * Example: 001-011-5838423-001
      */
    case class AccountID(value: String) extends AnyVal

    /**
      * The full details of a client’s Account. This includes full open Trade, open Position and pending Order representation.
      *
      * @param id                          The Account’s identifier
      * @param alias                       Client-assigned alias for the Account. Only provided if the Account has an alias set
      * @param currency                    The home currency of the Account
      * @param balance                     The current balance of the Account. Represented in the Account’s home currency.
      * @param createdByUserID             ID of the user that created the Account.
      * @param createdTime                 The date/time when the Account was created.
      * @param pl                          The total profit/loss realized over the lifetime of the Account. Represented in the Account’s home currency.
      * @param resettablePL                The total realized profit/loss for the Account since it was last reset by the client. Represented in the Account’s home currency.
      * @param resettabledPLTime           The date/time that the Account’s resettablePL was last reset.
      * @param marginRate                  Client-provided margin rate override for the Account. The effective margin rate of the Account is the lesser of this value and the OANDA margin rate for the Account’s division. This value is only provided if a margin rate override exists for the Account.
      * @param marginCallEnterTime         The date/time when the Account entered a margin call state. Only provided if the Account is in a margin call.
      * @param marginCallExtensionCount    The number of times that the Account’s current margin call was extended.
      * @param lastMarginCallExtensionTime The date/time of the Account’s last margin call extension.
      * @param openTradeCount              The number of Trades currently open in the Account.
      * @param openPositionCount           The number of Positions currently open in the Account.
      * @param pendingOrderCount           The number of Orders currently pending in the Account.
      * @param hedgingEnabled              Flag indicating that the Account has hedging enabled.
      * @param unrealizedPL                The total unrealized profit/loss for all Trades currently open in the Account. Represented in the Account’s home currency.
      * @param NAV                         The net asset value of the Account. Equal to Account balance + unrealizedPL. Represented in the Account’s home currency.
      * @param marginUsed                  Margin currently used for the Account. Represented in the Account’s home currency.
      * @param marginAvailable             Margin available for Account. Represented in the Account’s home currency.
      * @param positionValue               The value of the Account’s open positions represented in the Account’s home currency.
      * @param marginCloseoutUnrealizedPL  The Account’s margin closeout unrealized PL.
      * @param marginCloseoutNAV           The Account’s margin closeout NAV.
      * @param marginCloseoutMarginUsed    The Account’s margin closeout margin used.
      * @param marginCloseoutPercent       The Account’s margin closeout percentage. When this value is 1.0 or above the Account is in a margin closeout situation.
      * @param marginCloseoutPositionValue The value of the Account’s open positions as used for margin closeout calculations represented in the Account’s home currency.
      * @param withdrawalLimit             The current WithdrawalLimit for the account which will be zero or a positive value indicating how much can be withdrawn from the account.
      * @param marginCallMarginUsed        The Account’s margin call margin used.
      * @param marginCallPercent           The Account’s margin call percentage. When this value is 1.0 or above the Account is in a margin call situation.
      * @param lastTransactionID           The ID of the last Transaction created for the Account.
      * @param trades                      The details of the Trades currently open in the Account.
      * @param positions                   The details all Account Positions.
      * @param orders                      The details of the Orders currently pending in the Account.
      */
    case class Account(id: AccountID,
                       alias: String,
                       currency: Currency,
                       balance: AccountUnits,
                       createdByUserID: Int,
                       createdTime: DateTime,
                       pl: AccountUnits,
                       resettablePL: AccountUnits,
                       resettabledPLTime: Option[DateTime],
                       marginRate: Double,
                       marginCallEnterTime: Option[DateTime],
                       marginCallExtensionCount: Option[Int],
                       lastMarginCallExtensionTime: Option[DateTime],
                       openTradeCount: Int,
                       openPositionCount: Int,
                       pendingOrderCount: Int,
                       hedgingEnabled: Boolean,
                       unrealizedPL: AccountUnits,
                       NAV: AccountUnits,
                       marginUsed: AccountUnits,
                       marginAvailable: AccountUnits,
                       positionValue: AccountUnits,
                       marginCloseoutUnrealizedPL: AccountUnits,
                       marginCloseoutNAV: AccountUnits,
                       marginCloseoutMarginUsed: AccountUnits,
                       marginCloseoutPercent: Double,
                       marginCloseoutPositionValue: Double,
                       withdrawalLimit: AccountUnits,
                       marginCallMarginUsed: AccountUnits,
                       marginCallPercent: Double,
                       lastTransactionID: TransactionID,
                       trades: Seq[TradeSummary],
                       positions: Seq[Position],
                       orders: Seq[Order])

    /**
      * An AccountState Object is used to represent an Account’s current price-dependent state. Price-dependent Account state is dependent on OANDA’s current Prices, and includes things like unrealized PL, NAV and Trailing Stop Loss Order state.
      *
      * @param unrealizedPL                The total unrealized profit/loss for all Trades currently open in the Account. Represented in the Account’s home currency.
      * @param NAV                         The net asset value of the Account. Equal to Account balance + unrealizedPL. Represented in the Account’s home currency.
      * @param marginUsed                  Margin currently used for the Account. Represented in the Account’s home currency.
      * @param marginAvailable             Margin available for Account. Represented in the Account’s home currency.
      * @param positionValue               The value of the Account’s open positions represented in the Account’s home currency.
      * @param marginCloseoutUnrealizedPL  The Account’s margin closeout unrealized PL.
      * @param marginCloseoutNAV           The Account’s margin closeout NAV.
      * @param marginCloseoutMarginUsed    The Account’s margin closeout margin used.
      * @param marginCloseoutPercent       The Account’s margin closeout percentage. When this value is 1.0 or above the Account is in a margin closeout situation.
      * @param marginCloseoutPositionValue The value of the Account’s open positions as used for margin closeout calculations represented in the Account’s home currency.
      * @param withdrawalLimit             The current WithdrawalLimit for the account which will be zero or a positive value indicating how much can be withdrawn from the account.
      * @param marginCallMarginUsed        The Account’s margin call margin used.
      * @param marginCallPercent           The Account’s margin call percentage. When this value is 1.0 or above the Account is in a margin call situation.
      * @param orders                      The price-dependent state of each pending Order in the Account.
      * @param trades                      The price-dependent state for each open Trade in the Account.
      * @param positions                   The price-dependent state for each open Position in the Account.
      */
    case class AccountChangesState(unrealizedPL: AccountUnits,
                                   NAV: AccountUnits,
                                   marginUsed: AccountUnits,
                                   marginAvailable: AccountUnits,
                                   positionValue: AccountUnits,
                                   marginCloseoutUnrealizedPL: AccountUnits,
                                   marginCloseoutNAV: AccountUnits,
                                   marginCloseoutMarginUsed: AccountUnits,
                                   marginCloseoutPercent: Double,
                                   marginCloseoutPositionValue: Double,
                                   withdrawalLimit: AccountUnits,
                                   marginCallMarginUsed: AccountUnits,
                                   marginCallPercent: Double,
                                   orders: Seq[DynamicOrderState],
                                   trades: Seq[CalculatedTradeState],
                                   positions: Seq[CalculatedPositionState])

    /**
      * Properties related to an Account.
      *
      * @param id           The Account’s identifier
      * @param mt4AccountID The Account’s associated MT4 Account ID. This field will not be present if the Account is not an MT4 account.
      * @param tags         The Account’s tags
      */
    case class AccountProperties(id: AccountID,
                                 mt4AccountID: Option[Int],
                                 tags: Seq[String])

    /**
      * A summary representation of a client’s Account. The AccountSummary does not provide to full specification of pending Orders, open Trades and Positions.
      *
      * @param id                          The Account’s identifier
      * @param alias                       Client-assigned alias for the Account. Only provided if the Account has an alias set
      * @param currency                    The home currency of the Account
      * @param balance                     The current balance of the Account. Represented in the Account’s home currency.
      * @param createdByUserID             ID of the user that created the Account.
      * @param createdTime                 The date/time when the Account was created.
      * @param pl                          The total profit/loss realized over the lifetime of the Account. Represented in the Account’s home currency.
      * @param resettablePL                The total realized profit/loss for the Account since it was last reset by the client. Represented in the Account’s home currency.
      * @param resettabledPLTime           The date/time that the Account’s resettablePL was last reset.
      * @param marginRate                  Client-provided margin rate override for the Account. The effective margin rate of the Account is the lesser of this value and the OANDA margin rate for the Account’s division. This value is only provided if a margin rate override exists for the Account.
      * @param marginCallEnterTime         The date/time when the Account entered a margin call state. Only provided if the Account is in a margin call.
      * @param marginCallExtensionCount    The number of times that the Account’s current margin call was extended.
      * @param lastMarginCallExtensionTime The date/time of the Account’s last margin call extension.
      * @param openTradeCount              The number of Trades currently open in the Account.
      * @param openPositionCount           The number of Positions currently open in the Account.
      * @param pendingOrderCount           The number of Orders currently pending in the Account.
      * @param hedgingEnabled              Flag indicating that the Account has hedging enabled.
      * @param unrealizedPL                The total unrealized profit/loss for all Trades currently open in the Account. Represented in the Account’s home currency.
      * @param NAV                         The net asset value of the Account. Equal to Account balance + unrealizedPL. Represented in the Account’s home currency.
      * @param marginUsed                  Margin currently used for the Account. Represented in the Account’s home currency.
      * @param marginAvailable             Margin available for Account. Represented in the Account’s home currency.
      * @param positionValue               The value of the Account’s open positions represented in the Account’s home currency.
      * @param marginCloseoutUnrealizedPL  The Account’s margin closeout unrealized PL.
      * @param marginCloseoutNAV           The Account’s margin closeout NAV.
      * @param marginCloseoutMarginUsed    The Account’s margin closeout margin used.
      * @param marginCloseoutPercent       The Account’s margin closeout percentage. When this value is 1.0 or above the Account is in a margin closeout situation.
      * @param marginCloseoutPositionValue The value of the Account’s open positions as used for margin closeout calculations represented in the Account’s home currency.
      * @param withdrawalLimit             The current WithdrawalLimit for the account which will be zero or a positive value indicating how much can be withdrawn from the account.
      * @param marginCallMarginUsed        The Account’s margin call margin used.
      * @param marginCallPercent           The Account’s margin call percentage. When this value is 1.0 or above the Account is in a margin call situation.
      * @param lastTransactionID           The ID of the last Transaction created for the Account.
      */
    case class AccountSummary(id: AccountID,
                              alias: String,
                              currency: Currency,
                              balance: AccountUnits,
                              createdByUserID: Int,
                              createdTime: DateTime,
                              pl: AccountUnits,
                              resettablePL: AccountUnits,
                              resettabledPLTime: Option[DateTime],
                              marginRate: Double,
                              marginCallEnterTime: Option[DateTime],
                              marginCallExtensionCount: Option[Int],
                              lastMarginCallExtensionTime: Option[DateTime],
                              openTradeCount: Int,
                              openPositionCount: Int,
                              pendingOrderCount: Int,
                              hedgingEnabled: Boolean,
                              unrealizedPL: AccountUnits,
                              NAV: AccountUnits,
                              marginUsed: AccountUnits,
                              marginAvailable: AccountUnits,
                              positionValue: AccountUnits,
                              marginCloseoutUnrealizedPL: AccountUnits,
                              marginCloseoutNAV: AccountUnits,
                              marginCloseoutMarginUsed: AccountUnits,
                              marginCloseoutPercent: Double,
                              marginCloseoutPositionValue: Double,
                              withdrawalLimit: AccountUnits,
                              marginCallMarginUsed: AccountUnits,
                              marginCallPercent: Double,
                              lastTransactionID: TransactionID)

    /**
      * An AccountChanges Object is used to represent the changes to an Account’s Orders, Trades and Positions since a specified Account TransactionID in the past.
      *
      * @param ordersCreated   The Orders created. These Orders may have been filled, cancelled or triggered in the same period.
      * @param ordersCancelled The Orders cancelled.
      * @param ordersFilled    The Orders filled.
      * @param ordersTriggered The Orders triggered.
      * @param tradesOpened    The Trades opened.
      * @param tradesReduced   The Trades reduced.
      * @param tradesClosed    The Trades closed.
      * @param positions       The Positions changed.
      * @param transactions    The Transactions that have been generated.
      */
    case class AccountChanges(ordersCreated: Seq[Order],
                              ordersCancelled: Seq[Order],
                              ordersFilled: Seq[Order],
                              ordersTriggered: Seq[Order],
                              tradesOpened: Seq[TradeSummary],
                              tradesReduced: Seq[TradeSummary],
                              tradesClosed: Seq[TradeSummary],
                              positions: Seq[Position],
                              transactions: Seq[Transaction])

    /**
      * The financing mode of an Account
      */
    object AccountFinancingMode extends Enumeration {
      type AccountFinancingMode = Value

      /** No financing is paid/charged for open Trades in the Account */
      val NO_FINANCING = Value

      /** Second-by-second financing is paid/charged for open Trades in the Account, both daily and when the the Trade is closed */
      val SECOND_BY_SECOND = Value

      /** A full day’s worth of financing is paid/charged for open Trades in the Account daily at 5pm New York time */
      val DAILY = Value
    }

    /**
      * The way that position values for an Account are calculated and aggregated.
      */
    object PositionAggregationMode extends Enumeration {
      type PositionAggregationMode = Value

      /** The Position value or margin for each side (long and short) of the Position are computed independently and added together. */
      val ABSOLUTE_SUM = Value

      /** The Position value or margin for each side (long and short) of the Position are computed independently. The Position value or margin chosen is the maximal absolute value of the two. */
      val MAXIMAL_SIDE = Value

      /** The units for each side (long and short) of the Position are netted together and the resulting value (long or short) is used to compute the Position value or margin. */
      val NET_SUM = Value
    }

  }

  object InstrumentModel {

    /**
      * The granularity of a candlestick
      */
    object CandlestickGranularity extends Enumeration {
      type CandlestickGranularity = Value

      /** 30 second candlesticks, minute alignment */
      val S30 = Value

      /** 2 hour candlesticks, day alignment */
      val H2 = Value

      /** 15 second candlesticks, minute alignment */
      val S15 = Value

      /** 2 minute candlesticks, hour alignment */
      val M2 = Value

      /** 1 hour candlesticks, hour alignment */
      val H1 = Value

      /** 6 hour candlesticks, day alignment */
      val H6 = Value

      /** 10 second candlesticks, minute alignment */
      val S10 = Value

      /** 1 month candlesticks, aligned to first day of the month */
      val M = Value

      /** 5 minute candlesticks, hour alignment */
      val M5 = Value

      /** 4 hour candlesticks, day alignment */
      val H4 = Value

      /** 4 minute candlesticks, hour alignment */
      val M4 = Value

      /** 8 hour candlesticks, day alignment */
      val H8 = Value

      /** 10 minute candlesticks, hour alignment */
      val M10 = Value

      /** 15 minute candlesticks, hour alignment */
      val M15 = Value

      /** 12 hour candlesticks, day alignment */
      val H12 = Value

      /** 5 second candlesticks, minute alignment */
      val S5 = Value

      /** 1 week candlesticks, aligned to start of week */
      val W = Value

      /** 3 hour candlesticks, day alignment */
      val H3 = Value

      /** 1 day candlesticks, day alignment */
      val D = Value

      /** 30 minute candlesticks, hour alignment */
      val M30 = Value

      /** 1 minute candlesticks, minute alignment */
      val M1 = Value
    }

    /**
      * The day of the week to use for candlestick granularities with weekly alignment.
      */
    object WeeklyAlignment extends Enumeration {
      type WeeklyAlignment = Value

      /** Wednesday */
      val Wednesday = Value

      /** Monday */
      val Monday = Value

      /** Saturday */
      val Saturday = Value

      /** Thursday */
      val Thursday = Value

      /** Tuesday */
      val Tuesday = Value

      /** Friday */
      val Friday = Value

      /** Sunday */
      val Sunday = Value
    }

    /**
      * The Candlestick representation
      *
      * @param time     The start time of the candlestick
      * @param bid      The candlestick data based on bids. Only provided if bid-based candles were requested.
      * @param ask      The candlestick data based on asks. Only provided if ask-based candles were requested.
      * @param mid      The candlestick data based on midpoints. Only provided if midpoint-based candles were requested.
      * @param volume   The number of prices created during the time-range represented by the candlestick.
      * @param complete A flag indicating if the candlestick is complete. A complete candlestick is one whose ending time is not in the future.
      */
    case class Candlestick(time: DateTime,
                           bid: Option[CandlestickData],
                           ask: Option[CandlestickData],
                           mid: Option[CandlestickData],
                           volume: Int,
                           complete: Boolean)

    /**
      * The price data (open, high, low, close) for the Candlestick representation.
      *
      * @param o The first (open) price in the time-range represented by the candlestick.
      * @param h The highest price in the time-range represented by the candlestick.
      * @param l The lowest price in the time-range represented by the candlestick.
      * @param c The last (closing) price in the time-range represented by the candlestick.
      */
    case class CandlestickData(o: PriceValue,
                               h: PriceValue,
                               l: PriceValue,
                               c: PriceValue)

  }

  object OrderModel {

    /**
      * The base Order definition specifies the properties that are common to all Orders.
      *
      * @param id               The Order’s identifier, unique within the Order’s Account.
      * @param createTime       The time when the Order was created.
      * @param state            The current state of the Order.
      * @param clientExtensions The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type             The type of the Order.
      */
    abstract class Order(id: OrderID,
                         createTime: DateTime,
                         state: OrderState,
                         clientExtensions: Option[ClientExtensions],
                         `type`: OrderType)

    /**
      * A MarketOrder is an order that is filled immediately upon creation using the current market price.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "MARKET" for Market Orders.
      * @param instrument              The Market Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Market Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param timeInForce             The time-in-force requested for the Market Order. Restricted to FOK or IOC for a MarketOrder.
      * @param priceBound              The worst price that the client is willing to have the Market Order filled at.
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param tradeClose              Details of the Trade requested to be closed, only provided when the Market Order is being used to explicitly close a Trade.
      * @param longPositionCloseout    Details of the long Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a long Position.
      * @param shortPositionCloseout   Details of the short Position requested to be closed out, only provided when a Market Order is being used to explicitly closeout a short Position.
      * @param marginCloseout          Details of the Margin Closeout that this Market Order was created for
      * @param delayedTradeClose       Details of the delayed Trade close that this Market Order was created for
      * @param takeProfitOnFill        TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill          StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill  TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      */
    case class MarketOrder(id: OrderID,
                           createTime: DateTime,
                           state: OrderState,
                           clientExtensions: Option[ClientExtensions],
                           `type`: OrderType = OrderType.MARKET,
                           instrument: InstrumentName,
                           units: Double,
                           timeInForce: TimeInForce,
                           priceBound: Option[PriceValue],
                           positionFill: OrderPositionFill,
                           tradeClose: Option[MarketOrderTradeClose],
                           longPositionCloseout: Option[MarketOrderPositionCloseout],
                           shortPositionCloseout: Option[MarketOrderPositionCloseout],
                           marginCloseout: Option[MarketOrderMarginCloseout],
                           delayedTradeClose: Option[MarketOrderDelayedTradeClose],
                           takeProfitOnFill: Option[TakeProfitDetails],
                           stopLossOnFill: Option[StopLossDetails],
                           trailingStopLossOnFill: Option[TrailingStopLossDetails],
                           tradeclientExtensions: Option[ClientExtensions],
                           fillingTransactionID: Option[TransactionID],
                           filledTime: Option[DateTime],
                           tradeOpenedID: Option[TradeID],
                           tradeReducedID: Option[TradeID],
                           tradeClosedIDs: Seq[TradeID],
                           cancellingTransactionID: Option[TransactionID],
                           cancelledTime: Option[DateTime]
                          ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A LimitOrder is an order that is created with a price threshold, and will only be filled by a price that is equal to or better than the threshold.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "LIMIT" for Limit Orders.
      * @param instrument              The Limit Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Limit Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Limit Order. The Limit Order will only be filled by a market price that is equal to or better than this price.
      * @param timeInForce             The time-in-force requested for the Limit Order.
      * @param gtdTime                 The date/time when the Limit Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param takeProfitOnFill        TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill          StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill  TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class LimitOrder(id: OrderID,
                          createTime: DateTime,
                          state: OrderState,
                          clientExtensions: Option[ClientExtensions],
                          `type`: OrderType = OrderType.LIMIT,
                          instrument: InstrumentName,
                          units: Double,
                          price: PriceValue,
                          timeInForce: TimeInForce,
                          gtdTime: Option[DateTime],
                          positionFill: OrderPositionFill,
                          triggerCondition: OrderTriggerCondition,
                          takeProfitOnFill: Option[TakeProfitDetails],
                          stopLossOnFill: Option[StopLossDetails],
                          trailingStopLossOnFill: Option[TrailingStopLossDetails],
                          tradeclientExtensions: Option[ClientExtensions],
                          fillingTransactionID: Option[TransactionID],
                          filledTime: Option[DateTime],
                          tradeOpenedID: Option[TradeID],
                          tradeReducedID: Option[TradeID],
                          tradeClosedIDs: Seq[TradeID],
                          cancellingTransactionID: Option[TransactionID],
                          cancelledTime: Option[DateTime],
                          replacesOrderID: Option[OrderID],
                          replacedByOrderID: Option[OrderID]
                         ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A StopOrder is an order that is created with a price threshold, and will only be filled by a price that is equal to or worse than the threshold.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "STOP" for Stop Orders.
      * @param instrument              The Stop Order’s Instrument.
      * @param units                   The quantity requested to be filled by the Stop Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the Stop Order. The Stop Order will only be filled by a market price that is equal to or worse than this price.
      * @param priceBound              The worst market price that may be used to fill this Stop Order. If the market gaps and crosses through both the price and the priceBound, the Stop Order will be cancelled instead of being filled.
      * @param timeInForce             The time-in-force requested for the Stop Order.
      * @param gtdTime                 The date/time when the Stop Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param takeProfitOnFill        TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill          StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill  TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class StopOrder(id: OrderID,
                         createTime: DateTime,
                         state: OrderState,
                         clientExtensions: Option[ClientExtensions],
                         `type`: OrderType = OrderType.STOP,
                         instrument: InstrumentName,
                         units: Double,
                         price: PriceValue,
                         priceBound: Option[PriceValue],
                         timeInForce: TimeInForce,
                         gtdTime: Option[DateTime],
                         positionFill: OrderPositionFill,
                         triggerCondition: OrderTriggerCondition,
                         takeProfitOnFill: Option[TakeProfitDetails],
                         stopLossOnFill: Option[StopLossDetails],
                         trailingStopLossOnFill: Option[TrailingStopLossDetails],
                         tradeclientExtensions: Option[ClientExtensions],
                         fillingTransactionID: Option[TransactionID],
                         filledTime: Option[DateTime],
                         tradeOpenedID: Option[TradeID],
                         tradeReducedID: Option[TradeID],
                         tradeClosedIDs: Seq[TradeID],
                         cancellingTransactionID: Option[TransactionID],
                         cancelledTime: Option[DateTime],
                         replacesOrderID: Option[OrderID],
                         replacedByOrderID: Option[OrderID]
                        ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A MarketIfTouchedOrder is an order that is created with a price threshold, and will only be filled by a market price that is touches or crosses the threshold.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "MARKET_IF_TOUCHED" for Market If Touched Orders.
      * @param instrument              The MarketIfTouched Order’s Instrument.
      * @param units                   The quantity requested to be filled by the MarketIfTouched Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                   The price threshold specified for the MarketIfTouched Order. The MarketIfTouched Order will only be filled by a market price that crosses this price from the direction of the market price at the time when the Order was created (the initialMarketPrice). Depending on the value of the Order’s price and initialMarketPrice, the MarketIfTouchedOrder will behave like a Limit or a Stop Order.
      * @param priceBound              The worst market price that may be used to fill this MarketIfTouched Order.
      * @param timeInForce             The time-in-force requested for the MarketIfTouched Order. Restricted to "GTC", "GFD" and "GTD" for MarketIfTouched Orders.
      * @param gtdTime                 The date/time when the MarketIfTouched Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill            Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param initialMarketPrice      The Market price at the time when the MarketIfTouched Order was created.
      * @param takeProfitOnFill        TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill          StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill  TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions   Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class MarketIfTouchedOrder(id: OrderID,
                                    createTime: DateTime,
                                    state: OrderState,
                                    clientExtensions: Option[ClientExtensions],
                                    `type`: OrderType = OrderType.MARKET_IF_TOUCHED,
                                    instrument: InstrumentName,
                                    units: Double,
                                    price: PriceValue,
                                    priceBound: Option[PriceValue],
                                    timeInForce: TimeInForce,
                                    gtdTime: Option[DateTime],
                                    positionFill: OrderPositionFill,
                                    triggerCondition: OrderTriggerCondition,
                                    initialMarketPrice: PriceValue,
                                    takeProfitOnFill: Option[TakeProfitDetails],
                                    stopLossOnFill: Option[StopLossDetails],
                                    trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                    tradeclientExtensions: Option[ClientExtensions],
                                    fillingTransactionID: Option[TransactionID],
                                    filledTime: Option[DateTime],
                                    tradeOpenedID: Option[TradeID],
                                    tradeReducedID: Option[TradeID],
                                    tradeClosedIDs: Seq[TradeID],
                                    cancellingTransactionID: Option[TransactionID],
                                    cancelledTime: Option[DateTime],
                                    replacesOrderID: Option[OrderID],
                                    replacedByOrderID: Option[OrderID]
                                   ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A TakeProfitOrder is an order that is linked to an open Trade and created with a price threshold. The Order will be filled (closing the Trade) by the first price that is equal to or better than the threshold. A TakeProfitOrder cannot be used to open a new Position.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "TAKE_PROFIT" for Take Profit Orders.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the TakeProfit Order. The associated Trade will be closed by a market price that is equal to or better than this threshold.
      * @param timeInForce             The time-in-force requested for the TakeProfit Order. Restricted to "GTC", "GFD" and "GTD" for TakeProfit Orders.
      * @param gtdTime                 The date/time when the TakeProfit Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class TakeProfitOrder(id: OrderID,
                               createTime: DateTime,
                               state: OrderState,
                               clientExtensions: Option[ClientExtensions],
                               `type`: OrderType = OrderType.TAKE_PROFIT,
                               tradeID: TradeID,
                               clientTradeID: Option[ClientID],
                               price: PriceValue,
                               timeInForce: TimeInForce,
                               gtdTime: Option[DateTime],
                               triggerCondition: OrderTriggerCondition,
                               fillingTransactionID: Option[TransactionID],
                               filledTime: Option[DateTime],
                               tradeOpenedID: Option[TradeID],
                               tradeReducedID: Option[TradeID],
                               tradeClosedIDs: Seq[TradeID],
                               cancellingTransactionID: Option[TransactionID],
                               cancelledTime: Option[DateTime],
                               replacesOrderID: Option[OrderID],
                               replacedByOrderID: Option[OrderID]
                              ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A StopLossOrder is an order that is linked to an open Trade and created with a price threshold. The Order will be filled (closing the Trade) by the first price that is equal to or worse than the threshold. A StopLossOrder cannot be used to open a new Position.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "STOP_LOSS" for Stop Loss Orders.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param price                   The price threshold specified for the StopLoss Order. The associated Trade will be closed by a market price that is equal to or worse than this threshold.
      * @param timeInForce             The time-in-force requested for the StopLoss Order. Restricted to "GTC", "GFD" and "GTD" for StopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class StopLossOrder(id: OrderID,
                             createTime: DateTime,
                             state: OrderState,
                             clientExtensions: Option[ClientExtensions],
                             `type`: OrderType = OrderType.STOP_LOSS,
                             tradeID: TradeID,
                             clientTradeID: Option[ClientID],
                             price: PriceValue,
                             timeInForce: TimeInForce,
                             gtdTime: Option[DateTime],
                             triggerCondition: OrderTriggerCondition,
                             fillingTransactionID: Option[TransactionID],
                             filledTime: Option[DateTime],
                             tradeOpenedID: Option[TradeID],
                             tradeReducedID: Option[TradeID],
                             tradeClosedIDs: Seq[TradeID],
                             cancellingTransactionID: Option[TransactionID],
                             cancelledTime: Option[DateTime],
                             replacesOrderID: Option[OrderID],
                             replacedByOrderID: Option[OrderID]
                            ) extends Order(id, createTime, state, clientExtensions, `type`)

    /**
      * A TrailingStopLossOrder is an order that is linked to an open Trade and created with a price distance. The price distance is used to calculate a trailing stop value for the order that is in the losing direction from the market price at the time of the order’s creation. The trailing stop value will follow the market price as it moves in the winning direction, and the order will filled (closing the Trade) by the first price that is equal to or worse than the trailing stop value. A TrailingStopLossOrder cannot be used to open a new Position.
      *
      * @param id                      The Order’s identifier, unique within the Order’s Account.
      * @param createTime              The time when the Order was created.
      * @param state                   The current state of the Order.
      * @param clientExtensions        The client extensions of the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param type                    The type of the Order. Always set to "TRAILING_STOP_LOSS" for Trailing Stop Loss Orders.
      * @param tradeID                 The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID           The client ID of the Trade to be closed when the price threshold is breached.
      * @param distance                The price distance specified for the TrailingStopLoss Order.
      * @param timeInForce             The time-in-force requested for the TrailingStopLoss Order. Restricted to "GTC", "GFD" and "GTD" for TrailingStopLoss Orders.
      * @param gtdTime                 The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition        Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param trailingStopValue       The trigger price for the Trailing Stop Loss Order. The trailing stop value will trail (follow) the market price by the TSL order’s configured "distance" as the market price moves in the winning direction. If the market price moves to a level that is equal to or worse than the trailing stop value, the order will be filled and the Trade will be closed.
      * @param fillingTransactionID    ID of the Transaction that filled this Order (only provided when the Order’s state is FILLED)
      * @param filledTime              Date/time when the Order was filled (only provided when the Order’s state is FILLED)
      * @param tradeOpenedID           Trade ID of Trade opened when the Order was filled (only provided when the Order’s state is FILLED and a Trade was opened as a result of the fill)
      * @param tradeReducedID          Trade ID of Trade reduced when the Order was filled (only provided when the Order’s state is FILLED and a Trade was reduced as a result of the fill)
      * @param tradeClosedIDs          Trade IDs of Trades closed when the Order was filled (only provided when the Order’s state is FILLED and one or more Trades were closed as a result of the fill)
      * @param cancellingTransactionID ID of the Transaction that cancelled the Order (only provided when the Order’s state is CANCELLED)
      * @param cancelledTime           Date/time when the Order was cancelled (only provided when the state of the Order is CANCELLED)
      * @param replacesOrderID         The ID of the Order that was replaced by this Order (only provided if this Order was created as part of a cancel/replace).
      * @param replacedByOrderID       The ID of the Order that replaced this Order (only provided if this Order was cancelled as part of a cancel/replace).
      */
    case class TrailingStopLossOrder(id: OrderID,
                                     createTime: DateTime,
                                     state: OrderState,
                                     clientExtensions: Option[ClientExtensions],
                                     `type`: OrderType = OrderType.TRAILING_STOP_LOSS,
                                     tradeID: TradeID,
                                     clientTradeID: Option[ClientID],
                                     distance: PriceValue,
                                     timeInForce: TimeInForce,
                                     gtdTime: Option[DateTime],
                                     triggerCondition: OrderTriggerCondition,
                                     trailingStopValue: PriceValue,
                                     fillingTransactionID: Option[TransactionID],
                                     filledTime: Option[DateTime],
                                     tradeOpenedID: Option[TradeID],
                                     tradeReducedID: Option[TradeID],
                                     tradeClosedIDs: Seq[TradeID],
                                     cancellingTransactionID: Option[TransactionID],
                                     cancelledTime: Option[DateTime],
                                     replacesOrderID: Option[OrderID],
                                     replacedByOrderID: Option[OrderID]
                                    ) extends Order(id, createTime, state, clientExtensions, `type`)

    abstract class OrderRequest()

    /**
      * A MarketOrderRequest specifies the parameters that may be set when creating a Market Order.
      *
      * @param type                   The type of the Order to Create. Must be set to "MARKET" when creating a Market Order.
      * @param instrument             The Market Order’s Instrument.
      * @param units                  The quantity requested to be filled by the Market Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param timeInForce            The time-in-force requested for the Market Order. Restricted to FOK or IOC for a MarketOrder.
      * @param priceBound             The worst price that the client is willing to have the Market Order filled at.
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param clientExtensions       The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param takeProfitOnFill       TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill         StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      */
    case class MarketOrderRequest(`type`: OrderType = OrderType.MARKET,
                                  instrument: InstrumentName,
                                  units: Double,
                                  timeInForce: TimeInForce = TimeInForce.FOK,
                                  priceBound: Option[PriceValue] = None,
                                  positionFill: OrderPositionFill = OrderPositionFill.DEFAULT,
                                  clientExtensions: Option[ClientExtensions] = None,
                                  takeProfitOnFill: Option[TakeProfitDetails] = None,
                                  stopLossOnFill: Option[StopLossDetails] = None,
                                  trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                  tradeclientExtensions: Option[ClientExtensions] = None
                                 ) extends OrderRequest

    /**
      * A LimitOrderRequest specifies the parameters that may be set when creating a Limit Order.
      *
      * @param type                   The type of the Order to Create. Must be set to "LIMIT" when creating a Market Order.
      * @param instrument             The Limit Order’s Instrument.
      * @param units                  The quantity requested to be filled by the Limit Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                  The price threshold specified for the Limit Order. The Limit Order will only be filled by a market price that is equal to or better than this price.
      * @param timeInForce            The time-in-force requested for the Limit Order.
      * @param gtdTime                The date/time when the Limit Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition       Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions       The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param takeProfitOnFill       TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill         StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      */
    case class LimitOrderRequest(`type`: OrderType = OrderType.LIMIT,
                                 instrument: InstrumentName,
                                 units: Double,
                                 price: PriceValue,
                                 timeInForce: TimeInForce = TimeInForce.GTC,
                                 gtdTime: Option[DateTime] = None,
                                 positionFill: OrderPositionFill = OrderPositionFill.DEFAULT,
                                 triggerCondition: OrderTriggerCondition = OrderTriggerCondition.DEFAULT,
                                 clientExtensions: Option[ClientExtensions] = None,
                                 takeProfitOnFill: Option[TakeProfitDetails] = None,
                                 stopLossOnFill: Option[StopLossDetails] = None,
                                 trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                 tradeclientExtensions: Option[ClientExtensions] = None
                                ) extends OrderRequest

    /**
      * A StopOrderRequest specifies the parameters that may be set when creating a Stop Order.
      *
      * @param type                   The type of the Order to Create. Must be set to "STOP" when creating a Stop Order.
      * @param instrument             The Stop Order’s Instrument.
      * @param units                  The quantity requested to be filled by the Stop Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                  The price threshold specified for the Stop Order. The Stop Order will only be filled by a market price that is equal to or worse than this price.
      * @param priceBound             The worst market price that may be used to fill this Stop Order. If the market gaps and crosses through both the price and the priceBound, the Stop Order will be cancelled instead of being filled.
      * @param timeInForce            The time-in-force requested for the Stop Order.
      * @param gtdTime                The date/time when the Stop Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition       Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions       The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param takeProfitOnFill       TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill         StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      */
    case class StopOrderRequest(`type`: OrderType = OrderType.STOP,
                                instrument: InstrumentName,
                                units: Double,
                                price: PriceValue,
                                priceBound: Option[PriceValue],
                                timeInForce: TimeInForce,
                                gtdTime: Option[DateTime],
                                positionFill: OrderPositionFill = OrderPositionFill.DEFAULT,
                                triggerCondition: OrderTriggerCondition = OrderTriggerCondition.DEFAULT,
                                clientExtensions: Option[ClientExtensions],
                                takeProfitOnFill: Option[TakeProfitDetails],
                                stopLossOnFill: Option[StopLossDetails],
                                trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                tradeclientExtensions: Option[ClientExtensions]
                               ) extends OrderRequest

    /**
      * A MarketIfTouchedOrderRequest specifies the parameters that may be set when creating a Market-if-Touched Order.
      *
      * @param type                   The type of the Order to Create. Must be set to "MARKET_IF_TOUCHED" when creating a Market If Touched Order.
      * @param instrument             The MarketIfTouched Order’s Instrument.
      * @param units                  The quantity requested to be filled by the MarketIfTouched Order. A posititive number of units results in a long Order, and a negative number of units results in a short Order.
      * @param price                  The price threshold specified for the MarketIfTouched Order. The MarketIfTouched Order will only be filled by a market price that crosses this price from the direction of the market price at the time when the Order was created (the initialMarketPrice). Depending on the value of the Order’s price and initialMarketPrice, the MarketIfTouchedOrder will behave like a Limit or a Stop Order.
      * @param priceBound             The worst market price that may be used to fill this MarketIfTouched Order.
      * @param timeInForce            The time-in-force requested for the MarketIfTouched Order. Restricted to "GTC", "GFD" and "GTD" for MarketIfTouched Orders.
      * @param gtdTime                The date/time when the MarketIfTouched Order will be cancelled if its timeInForce is "GTD".
      * @param positionFill           Specification of how Positions in the Account are modified when the Order is filled.
      * @param triggerCondition       Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions       The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      * @param takeProfitOnFill       TakeProfitDetails specifies the details of a Take Profit Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Take Profit, or when a Trade’s dependent Take Profit Order is modified directly through the Trade.
      * @param stopLossOnFill         StopLossDetails specifies the details of a Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Stop Loss, or when a Trade’s dependent Stop Loss Order is modified directly through the Trade.
      * @param trailingStopLossOnFill TrailingStopLossDetails specifies the details of a Trailing Stop Loss Order to be created on behalf of a client. This may happen when an Order is filled that opens a Trade requiring a Trailing Stop Loss, or when a Trade’s dependent Trailing Stop Loss Order is modified directly through the Trade.
      * @param tradeclientExtensions  Client Extensions to add to the Trade created when the Order is filled (if such a Trade is created). Do not set, modify, or delete tradeClientExtensions if your account is associated with MT4.
      */
    case class MarketIfTouchedOrderRequest(`type`: OrderType = OrderType.MARKET_IF_TOUCHED,
                                           instrument: InstrumentName,
                                           units: Double,
                                           price: PriceValue,
                                           priceBound: Option[PriceValue],
                                           timeInForce: TimeInForce,
                                           gtdTime: Option[DateTime],
                                           positionFill: OrderPositionFill,
                                           triggerCondition: OrderTriggerCondition,
                                           clientExtensions: Option[ClientExtensions],
                                           takeProfitOnFill: Option[TakeProfitDetails],
                                           stopLossOnFill: Option[StopLossDetails],
                                           trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                           tradeclientExtensions: Option[ClientExtensions]
                                          ) extends OrderRequest

    /**
      * A TakeProfitOrderRequest specifies the parameters that may be set when creating a Take Profit Order.
      *
      * @param type             The type of the Order to Create. Must be set to "TAKE_PROFIT" when creating a Take Profit Order.
      * @param tradeID          The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID    The client ID of the Trade to be closed when the price threshold is breached.
      * @param price            The price threshold specified for the TakeProfit Order. The associated Trade will be closed by a market price that is equal to or better than this threshold.
      * @param timeInForce      The time-in-force requested for the TakeProfit Order. Restricted to "GTC", "GFD" and "GTD" for TakeProfit Orders.
      * @param gtdTime          The date/time when the TakeProfit Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      */
    case class TakeProfitOrderRequest(`type`: OrderType = OrderType.TAKE_PROFIT,
                                      tradeID: TradeID,
                                      clientTradeID: Option[ClientID],
                                      price: PriceValue,
                                      timeInForce: TimeInForce,
                                      gtdTime: Option[DateTime],
                                      triggerCondition: OrderTriggerCondition,
                                      clientExtensions: Option[ClientExtensions]
                                     ) extends OrderRequest

    /**
      * A StopLossOrderRequest specifies the parameters that may be set when creating a Stop Loss Order.
      *
      * @param type             The type of the Order to Create. Must be set to "STOP_LOSS" when creating a Stop Loss Order.
      * @param tradeID          The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID    The client ID of the Trade to be closed when the price threshold is breached.
      * @param price            The price threshold specified for the StopLoss Order. The associated Trade will be closed by a market price that is equal to or worse than this threshold.
      * @param timeInForce      The time-in-force requested for the StopLoss Order. Restricted to "GTC", "GFD" and "GTD" for StopLoss Orders.
      * @param gtdTime          The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      */
    case class StopLossOrderRequest(`type`: OrderType = OrderType.STOP_LOSS,
                                    tradeID: TradeID,
                                    clientTradeID: Option[ClientID],
                                    price: PriceValue,
                                    timeInForce: TimeInForce,
                                    gtdTime: Option[DateTime],
                                    triggerCondition: OrderTriggerCondition,
                                    clientExtensions: Option[ClientExtensions]
                                   ) extends OrderRequest

    /**
      * A TrailingStopLossOrderRequest specifies the parameters that may be set when creating a Trailing Stop Loss Order.
      *
      * @param type             The type of the Order to Create. Must be set to "TRAILING_STOP_LOSS" when creating a Trailng Stop Loss Order.
      * @param tradeID          The ID of the Trade to close when the price threshold is breached.
      * @param clientTradeID    The client ID of the Trade to be closed when the price threshold is breached.
      * @param distance         The price distance specified for the TrailingStopLoss Order.
      * @param timeInForce      The time-in-force requested for the TrailingStopLoss Order. Restricted to "GTC", "GFD" and "GTD" for TrailingStopLoss Orders.
      * @param gtdTime          The date/time when the StopLoss Order will be cancelled if its timeInForce is "GTD".
      * @param triggerCondition Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      * @param clientExtensions The client extensions to add to the Order. Do not set, modify, or delete clientExtensions if your account is associated with MT4.
      */
    case class TrailingStopLossOrderRequest(`type`: OrderType = OrderType.TRAILING_STOP_LOSS,
                                            tradeID: TradeID,
                                            clientTradeID: Option[ClientID],
                                            distance: PriceValue,
                                            timeInForce: TimeInForce,
                                            gtdTime: Option[DateTime],
                                            triggerCondition: OrderTriggerCondition,
                                            clientExtensions: Option[ClientExtensions]
                                           ) extends OrderRequest

    /**
      * The Order’s identifier, unique within the Order’s Account.
      * Format: The string representation of the OANDA-assigned OrderID. OANDA-assigned OrderIDs are positive integers, and are derived from the TransactionID of the Transaction that created the Order.
      * Example: 1523
      */
    case class OrderID(value: String) extends AnyVal

    /**
      * The type of the Order.
      */
    object OrderType extends Enumeration {
      type OrderType = Value

      /** A Limit Order */
      val LIMIT = Value

      /** A Take Profit Order */
      val TAKE_PROFIT = Value

      /** A Stop Order */
      val STOP = Value

      /** A Stop Loss Order */
      val STOP_LOSS = Value

      /** A Trailing Stop Loss Order */
      val TRAILING_STOP_LOSS = Value

      /** A Market Order */
      val MARKET = Value

      /** A Market-if-touched Order */
      val MARKET_IF_TOUCHED = Value
    }

    /**
      * The current state of the Order.
      */
    object OrderState extends Enumeration {
      type OrderState = Value

      /** The Order is currently pending execution */
      val PENDING = Value

      /** The Order has been filled */
      val FILLED = Value

      /** The Order has been triggered */
      val TRIGGERED = Value

      /** The Order has been cancelled */
      val CANCELLED = Value
    }

    /**
      * An OrderIdentifier is used to refer to an Order, and contains both the OrderID and the ClientOrderID.
      *
      * @param orderID       The OANDA-assigned Order ID
      * @param clientOrderID The client-provided client Order ID
      */
    case class OrderIdentifier(orderID: OrderID,
                               clientOrderID: ClientID)

    /**
      * The specification of an Order as referred to by clients
      * Format: Either the Order’s OANDA-assigned OrderID or the Order’s client-provided ClientID prefixed by the "@" symbol
      * Example: 1523
      */
    case class OrderSpecifier(value: String) extends AnyVal

    /**
      * The time-in-force of an Order. TimeInForce describes how long an Order should remain pending before being automatically cancelled by the execution system.
      */
    object TimeInForce extends Enumeration {
      type TimeInForce = Value

      /** The Order must be immediately "Filled Or Killed" */
      val FOK = Value

      /** The Order must be "Immediatedly paritally filled Or Cancelled" */
      val IOC = Value

      /** The Order is "Good unTil Cancelled" */
      val GTC = Value

      /** The Order is "Good unTil Date" and will be cancelled at the provided time */
      val GTD = Value

      /** The Order is "Good For Day" and will be cancelled at 5pm New York time */
      val GFD = Value
    }

    /**
      * Specification of how Positions in the Account are modified when the Order is filled.
      */
    object OrderPositionFill extends Enumeration {
      type OrderPositionFill = Value

      /** When the Order is filled, only allow Positions to be opened or extended. */
      val OPEN_ONLY = Value

      /** When the Order is filled, always fully reduce an existing Position before opening a new Position. */
      val REDUCE_FIRST = Value

      /** When the Order is filled, only reduce an existing Position. */
      val REDUCE_ONLY = Value

      /** When the Order is filled, use REDUCE_FIRST behaviour for non-client hedging Accounts, and OPEN_ONLY behaviour for client hedging Accounts. */
      val DEFAULT = Value
    }

    /**
      * Specification of what component of a price should be used for comparison when determining if the Order should be filled.
      */
    object OrderTriggerCondition extends Enumeration {
      type OrderTriggerCondition = Value

      /** Trigger an Order by comparing its price to the bid regardless of whether it is long or short. */
      val BID = Value

      /** Trigger an Order by comparing its price to the ask regardless of whether it is long or short. */
      val ASK = Value

      /** Trigger an Order by comparing its price to the midpoint regardless of whether it is long or short. */
      val MID = Value

      /** Trigger an Order the "natural" way: compare its price to the ask for long Orders and bid for short Orders. */
      val DEFAULT = Value

      /** Trigger an Order the opposite of the "natural" way: compare its price the bid for long Orders and ask for short Orders. */
      val INVERSE = Value
    }

    /**
      * The dynamic state of an Order. This is only relevant to TrailingStopLoss Orders, as no other Order type has dynamic state.
      *
      * @param id                     The Order’s ID.
      * @param trailingStopValue      The Order’s calculated trailing stop value.
      * @param triggerDistance        The distance between the Trailing Stop Loss Order’s trailingStopValue and the current Market Price. This represents the distance (in price units) of the Order from a triggering price. If the distance could not be determined, this value will not be set.
      * @param isTriggerDistanceExact True if an exact trigger distance could be calculated. If false, it means the provided trigger distance is a best estimate. If the distance could not be determined, this value will not be set.
      */
    case class DynamicOrderState(id: OrderID,
                                 trailingStopValue: PriceValue,
                                 triggerDistance: PriceValue,
                                 isTriggerDistanceExact: Boolean)

  }

  object TradeModel {

    /**
      * The Trade’s identifier, unique within the Trade’s Account.
      * Format: The string representation of the OANDA-assigned TradeID. OANDA-assigned TradeIDs are positive integers, and are derived from the TransactionID of the Transaction that opened the Trade.
      * Example: 1523
      */
    case class TradeID(value: String) extends AnyVal

    /**
      * The current state of the Trade.
      */
    object TradeState extends Enumeration {
      type TradeState = Value

      /** The Trade is currently open */
      val OPEN = Value

      /** The Trade has been fully closed */
      val CLOSED = Value

      /** The Trade will be closed as soon as the trade’s instrument becomes tradeable */
      val CLOSE_WHEN_TRADEABLE = Value
    }

    /**
      * The identification of a Trade as referred to by clients
      * Format: Either the Trade’s OANDA-assigned TradeID or the Trade’s client-provided ClientID prefixed by the "@" symbol
      * Example: @my_trade_id
      */
    case class TradeSpecifier(value: String) extends AnyVal

    /**
      * The specification of a Trade within an Account. This includes the full representation of the Trade’s dependent Orders in addition to the IDs of those Orders.
      *
      * @param id                    The Trade’s identifier, unique within the Trade’s Account.
      * @param instrument            The Trade’s Instrument.
      * @param price                 The execution price of the Trade.
      * @param openTime              The date/time when the Trade was opened.
      * @param state                 The current state of the Trade.
      * @param initialUnits          The initial size of the Trade. Negative values indicate a short Trade, and positive values indicate a long Trade.
      * @param currentUnits          The number of units currently open for the Trade. This value is reduced to 0.0 as the Trade is closed.
      * @param realizedPL            The total profit/loss realized on the closed portion of the Trade.
      * @param unrealizedPL          The unrealized profit/loss on the open portion of the Trade.
      * @param averageClosePrice     The average closing price of the Trade. Only present if the Trade has been closed or reduced at least once.
      * @param closingTransactionIDs The IDs of the Transactions that have closed portions of this Trade.
      * @param financing             The financing paid/collected for this Trade.
      * @param closeTime             The date/time when the Trade was fully closed. Only provided for Trades whose state is CLOSED.
      * @param clientExtensions      The client extensions of the Trade.
      * @param takeProfitOrder       Full representation of the Trade’s Take Profit Order, only provided if such an Order exists.
      * @param stopLossOrder         Full representation of the Trade’s Stop Loss Order, only provided if such an Order exists.
      * @param trailingStopLossOrder Full representation of the Trade’s Trailing Stop Loss Order, only provided if such an Order exists.
      */
    case class Trade(id: TradeID,
                     instrument: InstrumentName,
                     price: PriceValue,
                     openTime: DateTime,
                     state: TradeState,
                     initialUnits: Double,
                     currentUnits: Double,
                     realizedPL: AccountUnits,
                     unrealizedPL: Option[AccountUnits],
                     averageClosePrice: PriceValue,
                     closingTransactionIDs: Seq[TransactionID],
                     financing: AccountUnits,
                     closeTime: DateTime,
                     clientExtensions: Option[ClientExtensions],
                     takeProfitOrder: Option[TakeProfitOrder],
                     stopLossOrder: Option[StopLossOrder],
                     trailingStopLossOrder: Option[TrailingStopLossOrder])

    /**
      * The summary of a Trade within an Account. This representation does not provide the full details of the Trade’s dependent Orders.
      *
      * @param id                      The Trade’s identifier, unique within the Trade’s Account.
      * @param instrument              The Trade’s Instrument.
      * @param price                   The execution price of the Trade.
      * @param openTime                The date/time when the Trade was opened.
      * @param state                   The current state of the Trade.
      * @param initialUnits            The initial size of the Trade. Negative values indicate a short Trade, and positive values indicate a long Trade.
      * @param currentUnits            The number of units currently open for the Trade. This value is reduced to 0.0 as the Trade is closed.
      * @param realizedPL              The total profit/loss realized on the closed portion of the Trade.
      * @param unrealizedPL            The unrealized profit/loss on the open portion of the Trade.
      * @param averageClosePrice       The average closing price of the Trade. Only present if the Trade has been closed or reduced at least once.
      * @param closingTransactionIDs   The IDs of the Transactions that have closed portions of this Trade.
      * @param financing               The financing paid/collected for this Trade.
      * @param closeTime               The date/time when the Trade was fully closed. Only provided for Trades whose state is CLOSED.
      * @param clientExtensions        The client extensions of the Trade.
      * @param takeProfitOrderID       ID of the Trade’s Take Profit Order, only provided if such an Order exists.
      * @param stopLossOrderID         ID of the Trade’s Stop Loss Order, only provided if such an Order exists.
      * @param trailingStopLossOrderID ID of the Trade’s Trailing Stop Loss Order, only provided if such an Order exists.
      */
    case class TradeSummary(id: TradeID,
                            instrument: InstrumentName,
                            price: PriceValue,
                            openTime: DateTime,
                            state: TradeState,
                            initialUnits: Double,
                            currentUnits: Double,
                            realizedPL: AccountUnits,
                            unrealizedPL: AccountUnits,
                            averageClosePrice: PriceValue,
                            closingTransactionIDs: Seq[TransactionID],
                            financing: AccountUnits,
                            closeTime: DateTime,
                            clientExtensions: Option[ClientExtensions],
                            takeProfitOrderID: OrderID,
                            stopLossOrderID: OrderID,
                            trailingStopLossOrderID: OrderID)

    /**
      * The dynamic (calculated) state of an open Trade
      *
      * @param id           The Trade’s ID.
      * @param unrealizedPL The Trade’s unrealized profit/loss.
      */
    case class CalculatedTradeState(id: TradeID,
                                    unrealizedPL: AccountUnits)

  }

  object PositionModel {

    /**
      * The specification of a Position within an Account.
      *
      * @param instrument   The Position’s Instrument.
      * @param pl           Profit/loss realized by the Position over the lifetime of the Account.
      * @param unrealizedPL The unrealized profit/loss of all open Trades that contribute to this Position.
      * @param resettablePL Profit/loss realized by the Position since the Account’s resettablePL was last reset by the client.
      * @param long         The details of the long side of the Position.
      * @param short        The details of the short side of the Position.
      */
    case class Position(instrument: InstrumentName,
                        pl: AccountUnits,
                        unrealizedPL: AccountUnits,
                        resettablePL: AccountUnits,
                        long: PositionSide,
                        short: PositionSide)

    /**
      * The representation of a Position for a single direction (long or short).
      *
      * @param units        Number of units in the position (negative value indicates short position, positive indicates long position).
      * @param averagePrice Volume-weighted average of the underlying Trade open prices for the Position.
      * @param tradeIDs     List of the open Trade IDs which contribute to the open Position.
      * @param pl           Profit/loss realized by the PositionSide over the lifetime of the Account.
      * @param unrealizedPL The unrealized profit/loss of all open Trades that contribute to this PositionSide.
      * @param resettablePL Profit/loss realized by the PositionSide since the Account’s resettablePL was last reset by the client.
      */
    case class PositionSide(units: Double,
                            averagePrice: Option[PriceValue],
                            tradeIDs: Seq[TradeID],
                            pl: AccountUnits,
                            unrealizedPL: AccountUnits,
                            resettablePL: AccountUnits)

    /**
      * The dynamic (calculated) state of a Position
      *
      * @param instrument        The Position’s Instrument.
      * @param netUnrealizedPL   The Position’s net unrealized profit/loss
      * @param longUnrealizedPL  The unrealized profit/loss of the Position’s long open Trades
      * @param shortUnrealizedPL The unrealized profit/loss of the Position’s short open Trades
      */
    case class CalculatedPositionState(instrument: InstrumentName,
                                       netUnrealizedPL: AccountUnits,
                                       longUnrealizedPL: AccountUnits,
                                       shortUnrealizedPL: AccountUnits)

  }

  object PricingModel {

    /**
      * The specification of an Account-specific Price.
      *
      * @param type                       The string "PRICE". Used to identify the a Price object when found in a stream.
      * @param instrument                 The Price’s Instrument.
      * @param time                       The date/time when the Price was created
      * @param status                     The status of the Price.<b>Deprecated</b>: Will be removed in a future API update.
      * @param tradeable                  Flag indicating if the Price is tradeable or not
      * @param bids                       The list of prices and liquidity available on the Instrument’s bid side. It is possible for this list to be empty if there is no bid liquidity currently available for the Instrument in the Account.
      * @param asks                       The list of prices and liquidity available on the Instrument’s ask side. It is possible for this list to be empty if there is no ask liquidity currently available for the Instrument in the Account.
      * @param closeoutBid                The closeout bid Price. This Price is used when a bid is required to closeout a Position (margin closeout or manual) yet there is no bid liquidity. The closeout bid is never used to open a new position.
      * @param closeoutAsk                The closeout ask Price. This Price is used when a ask is required to closeout a Position (margin closeout or manual) yet there is no ask liquidity. The closeout ask is never used to open a new position.
      * @param quoteHomeConversionFactors The factors used to convert quantities of this price’s Instrument’s quote currency into a quantity of the Account’s home currency.<b>Deprecated</b>: Will be removed in a future API update.
      * @param unitsAvailable             Representation of how many units of an Instrument are available to be traded by an Order depending on its postionFill option.<b>Deprecated</b>: Will be removed in a future API update.
      */
    case class Price(`type`: String = "PRICE",
                     instrument: InstrumentName,
                     time: DateTime,
                     status: Option[PriceStatus] = None,
                     tradeable: Boolean,
                     bids: Seq[PriceBucket],
                     asks: Seq[PriceBucket],
                     closeoutBid: PriceValue,
                     closeoutAsk: PriceValue,
                     quoteHomeConversionFactors: Option[QuoteHomeConversionFactors],
                     unitsAvailable: Option[UnitsAvailable])

    /**
      * The string representation of a Price for an Instrument.
      * Format: A decimal number encodes as a string. The amount of precision provided depends on the Price’s Instrument.
      */
    case class PriceValue(value: BigDecimal) extends AnyVal {
      def pips = (value * 100000).toInt
    }

    /**
      * A Price Bucket represents a price available for an amount of liquidity
      *
      * @param price     The Price offered by the PriceBucket
      * @param liquidity The amount of liquidity offered by the PriceBucket
      */
    case class PriceBucket(price: PriceValue,
                           liquidity: Int)

    /**
      * The status of the Price.
      */
    object PriceStatus extends Enumeration {
      type PriceStatus = Value

      /** The Instrument’s price is tradeable. */
      val tradeable = Value

      /** The Instrument’s price is not tradeable. */
      val non_tradeable = Value

      /** The Instrument of the price is invalid or there is no valid Price for the Instrument. */
      val invalid = Value
    }

    /**
      * Representation of many units of an Instrument are available to be traded for both long and short Orders.
      *
      * @param long  The units available for long Orders.
      * @param short The units available for short Orders.
      */
    case class UnitsAvailableDetails(long: Double,
                                     short: Double)

    /**
      * Representation of how many units of an Instrument are available to be traded by an Order depending on its postionFill option.
      *
      * @param default     The number of units that are available to be traded using an Order with a positionFill option of "DEFAULT". For an Account with hedging enabled, this value will be the same as the "OPEN_ONLY" value. For an Account without hedging enabled, this value will be the same as the "REDUCE_FIRST" value.
      * @param reduceFirst The number of units that may are available to be traded with an Order with a positionFill option of "REDUCE_FIRST".
      * @param reduceOnly  The number of units that may are available to be traded with an Order with a positionFill option of "REDUCE_ONLY".
      * @param openOnly    The number of units that may are available to be traded with an Order with a positionFill option of "OPEN_ONLY".
      */
    case class UnitsAvailable(default: UnitsAvailableDetails,
                              reduceFirst: UnitsAvailableDetails,
                              reduceOnly: UnitsAvailableDetails,
                              openOnly: UnitsAvailableDetails)

    /**
      * QuoteHomeConversionFactors represents the factors that can be used used to convert quantities of a Price’s Instrument’s quote currency into the Account’s home currency.
      *
      * @param positiveUnits The factor used to convert a positive amount of the Price’s Instrument’s quote currency into a positive amount of the Account’s home currency. Conversion is performed by multiplying the quote units by the conversion factor.
      * @param negativeUnits The factor used to convert a negative amount of the Price’s Instrument’s quote currency into a negative amount of the Account’s home currency. Conversion is performed by multiplying the quote units by the conversion factor.
      */
    case class QuoteHomeConversionFactors(positiveUnits: Double,
                                          negativeUnits: Double)

    /**
      * A PricingHeartbeat object is injected into the Pricing stream to ensure that the HTTP connection remains active.
      *
      * @param type The string "HEARTBEAT"
      * @param time The date/time when the Heartbeat was created.
      */
    case class PricingHeartbeat(`type`: String = "PRICING_HEARTBEAT",
                                time: DateTime)

  }

  object PrimitivesModel {

    /**
      * The string representation of a decimal number.
      * Format: A decimal number encoded as a string. The amount of precision provided depends on what the number represents.
      */
    case class DecimalNumber(value: String) extends AnyVal

    /**
      * The string representation of a quantity of an Account’s home currency.
      * Format: A decimal number encoded as a string. The amount of precision provided depends on the Account’s home currency.
      */
    case class AccountUnits(value: String) extends AnyVal

    /**
      * Currency name identifier. Used by clients to refer to currencies.
      * Format: A string containing an ISO 4217 currency (
      */
    case class Currency(value: String) extends AnyVal

    /**
      * Instrument name identifier. Used by clients to refer to an Instrument.
      * Format: A string containing the base currency and quote currency delimited by a "_".
      */
    case class InstrumentName(value: String) extends AnyVal

    /**
      * The type of an Instrument.
      */
    object InstrumentType extends Enumeration {
      type InstrumentType = Value

      /** Currency */
      val CURRENCY = Value

      /** Contract For Difference */
      val CFD = Value

      /** Metal */
      val METAL = Value
    }

    /**
      * Full specification of an Instrument.
      *
      * @param name                        The name of the Instrument
      * @param type                        The type of the Instrument
      * @param displayName                 The display name of the Instrument
      * @param pipLocation                 The location of the "pip" for this instrument. The decimal position of the pip in this Instrument’s price can be found at 10 ^ pipLocation (e.g. -4 pipLocation results in a decimal pip position of 10 ^ -4 = 0.0001).
      * @param displayPrecision            The number of decimal places that should be used to display prices for this instrument. (e.g. a displayPrecision of 5 would result in a price of "1" being displayed as "1.00000")
      * @param tradeUnitsPrecision         The amount of decimal places that may be provided when specifying the number of units traded for this instrument.
      * @param minimumTradeSize            The smallest number of units allowed to be traded for this instrument.
      * @param maximumTrailingStopDistance The maximum trailing stop distance allowed for a trailing stop loss created for this instrument. Specified in price units.
      * @param minimumTrailingStopDistance The minimum trailing stop distance allowed for a trailing stop loss created for this instrument. Specified in price units.
      * @param maximumPositionSize         The maximum position size allowed for this instrument. Specified in units.
      * @param maximumOrderUnits           The maximum units allowed for an Order placed for this instrument. Specified in units.
      * @param marginRate                  The margin rate for this instrument.
      */
    case class Instrument(name: InstrumentName,
                          `type`: InstrumentType,
                          displayName: String,
                          pipLocation: Int,
                          displayPrecision: Int,
                          tradeUnitsPrecision: Int,
                          minimumTradeSize: Double,
                          maximumTrailingStopDistance: Double,
                          minimumTrailingStopDistance: Double,
                          maximumPositionSize: Double,
                          maximumOrderUnits: Double,
                          marginRate: Double)

    /**
      * A date and time value using either RFC3339 or UNIX time representation.
      * Format: The RFC 3339 representation is a string conforming to
      */
    case class DateTime(value: String) extends AnyVal {
      def toJodaDateTime = time.DateTime.parse(value)
    }

    /**
      * DateTime header
      */
    object AcceptDatetimeFormat extends Enumeration {
      type AcceptDatetimeFormat = Value

      /** If "UNIX" is specified DateTime fields will be specified or returned in the "12345678.000000123" format. */
      val UNIX = Value

      /** If "RFC3339" is specified DateTime will be specified or returned in "YYYY-MM-DDTHH:MM:SS.nnnnnnnnnZ" format. */
      val RFC3339 = Value
    }

  }

}
