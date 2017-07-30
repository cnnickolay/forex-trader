package org.nikosoft.oanda.api

import org.joda.time.DateTime
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.ext.EnumNameSerializer
import org.json4s.{CustomSerializer, DefaultFormats, _}
import org.nikosoft.oanda.api.ApiModel.AccountModel.AccountFinancingMode
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.CandlestickGranularity
import org.nikosoft.oanda.api.ApiModel.OrderModel._
import org.nikosoft.oanda.api.ApiModel.PricingModel.{Price, PriceStatus, PricingHeartbeat}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.InstrumentType
import org.nikosoft.oanda.api.ApiModel.TradeModel.TradeState
import org.nikosoft.oanda.api.ApiModel.TransactionModel._

import scala.math.BigDecimal.RoundingMode


object JsonSerializers {

  def transactionMappings: List[(String, Class[_])] = List(
    ("CREATE", classOf[CreateTransaction]),
    ("CLOSE", classOf[CloseTransaction]),
    ("REOPEN", classOf[ReopenTransaction]),
    ("CLIENT_CONFIGURE", classOf[ClientConfigureTransaction]),
    ("CLIENT_CONFIGURE_REJECT", classOf[ClientConfigureRejectTransaction]),
    ("TRANSFER_FUNDS", classOf[TransferFundsTransaction]),
    ("TRANSFER_FUNDS_REJECT", classOf[TransferFundsRejectTransaction]),
    ("MARKET_ORDER", classOf[MarketOrderTransaction]),
    ("MARKET_ORDER_REJECT", classOf[MarketOrderRejectTransaction]),
    ("LIMIT_ORDER", classOf[LimitOrderTransaction]),
    ("LIMIT_ORDER_REJECT", classOf[LimitOrderRejectTransaction]),
    ("STOP_ORDER", classOf[StopOrderTransaction]),
    ("STOP_ORDER_REJECT", classOf[StopOrderRejectTransaction]),
    ("MARKET_IF_TOUCHED_ORDER", classOf[MarketIfTouchedOrderTransaction]),
    ("MARKET_IF_TOUCHED_ORDER_REJECT", classOf[MarketIfTouchedOrderRejectTransaction]),
    ("TAKE_PROFIT_ORDER", classOf[TakeProfitOrderTransaction]),
    ("TAKE_PROFIT_ORDER_REJECT", classOf[TakeProfitOrderRejectTransaction]),
    ("STOP_LOSS_ORDER", classOf[StopLossOrderTransaction]),
    ("STOP_LOSS_ORDER_REJECT", classOf[StopLossOrderRejectTransaction]),
    ("TRAILING_STOP_LOSS_ORDER", classOf[TrailingStopLossOrderTransaction]),
    ("TRAILING_STOP_LOSS_ORDER_REJECT", classOf[TrailingStopLossOrderRejectTransaction]),
    ("ORDER_FILL", classOf[OrderFillTransaction]),
    ("ORDER_CANCEL", classOf[OrderCancelTransaction]),
    ("ORDER_CANCEL_REJECT", classOf[OrderCancelRejectTransaction]),
    ("ORDER_CLIENT_EXTENSIONS_MODIFY", classOf[OrderClientExtensionsModifyTransaction]),
    ("ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT", classOf[OrderClientExtensionsModifyRejectTransaction]),
    ("TRADE_CLIENT_EXTENSIONS_MODIFY", classOf[TradeClientExtensionsModifyTransaction]),
    ("TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT", classOf[TradeClientExtensionsModifyRejectTransaction]),
    ("MARGIN_CALL_ENTER", classOf[MarginCallEnterTransaction]),
    ("MARGIN_CALL_EXTEND", classOf[MarginCallExtendTransaction]),
    ("MARGIN_CALL_EXIT", classOf[MarginCallExitTransaction]),
    ("DELAYED_TRADE_CLOSURE", classOf[DelayedTradeClosureTransaction]),
    ("DAILY_FINANCING", classOf[DailyFinancingTransaction]),
    ("RESET_RESETTABLE_PL", classOf[ResetResettablePLTransaction])
  )
  val transactionMappingsByType: Map[String, Class[_]] = transactionMappings.toMap
  val transactionMappingsByClass: Map[Class[_], String] = transactionMappings.map(_.swap).toMap

  private object LongSerializer extends CustomSerializer[Long](format => ( {
    case JString(x) => x.toLong
    case JInt(x) => x.toLong
  }, {
    case x: Long => JInt(x)
    case x: BigDecimal => JString(x.toString)
  }))

  private object StringToDouble extends CustomSerializer[Double](format => ( {
    case JString(x) => x.toDouble
  }, {
    case x: Double => JString(x.toString)
  }))

  private object StringToBigDecimal extends CustomSerializer[BigDecimal](format => ( {
    case JString(x) => BigDecimal.valueOf(x.toDouble).setScale(5, RoundingMode.HALF_DOWN)
  }, {
    case x: BigDecimal => JString(x.toString)
  }))

  private object DateTimeSerializer extends CustomSerializer[DateTime](format => ( {
    case JString(date) => DateTime.parse(date)
  }, {
    case date: DateTime => JString(date.toString)
  }))

  def formatsHints: DefaultFormats = new DefaultFormats {
    override val typeHintFieldName: String = "type"
    override val typeHints: TypeHints = new TypeHints {
      override val hints: List[Class[_]] = List(
        classOf[MarketOrder],
        classOf[LimitOrder],
        classOf[StopOrder],
        classOf[MarketIfTouchedOrder],
        classOf[TakeProfitOrder],
        classOf[StopLossOrder],
        classOf[TrailingStopLossOrder],
        classOf[TransactionHeartbeat]
      ) ++ transactionMappings.map(_._2)

      override def classFor(hint: String): Option[Class[_]] = hint match {
        case "TRAILING_STOP_LOSS" => Option(classOf[TrailingStopLossOrder])
        case "STOP_LOSS" => Option(classOf[StopLossOrder])
        case "TAKE_PROFIT" => Option(classOf[TakeProfitOrder])
        case "MARKET_IF_TOUCHED" => Option(classOf[MarketIfTouchedOrder])
        case "STOP" => Option(classOf[StopOrder])
        case "LIMIT" => Option(classOf[LimitOrder])
        case "MARKET" => Option(classOf[MarketOrder])
        case "HEARTBEAT" => Option(classOf[TransactionHeartbeat])
        case "PRICING_HEARTBEAT" => Option(classOf[PricingHeartbeat])
        case "PRICE" => Option(classOf[Price])
        case t => transactionMappingsByType.get(t)
      }

      override def hintFor(clazz: Class[_]): String = clazz match {
        case _: Class[TrailingStopLossOrder] => "TRAILING_STOP_LOSS"
        case _: Class[StopLossOrder] => "STOP_LOSS"
        case _: Class[TakeProfitOrder] => "TAKE_PROFIT"
        case _: Class[MarketIfTouchedOrder] => "MARKET_IF_TOUCHED"
        case _: Class[StopOrder] => "STOP"
        case _: Class[LimitOrder] => "LIMIT"
        case _: Class[MarketOrder] => "MARKET"
        case _: Class[TransactionHeartbeat] => "HEARTBEAT"
        case _: Class[Price] => "PRICE"
        case _: Class[PricingHeartbeat] => "PRICING_HEARTBEAT"
        case c => transactionMappingsByClass(c)
      }
    }
  }

  private[oanda] def formats = formatsHints +
    StringToDouble +
    StringToBigDecimal +
    LongSerializer +
    DateTimeSerializer +
    new EnumNameSerializer(CandlestickGranularity) +
    new EnumNameSerializer(AccountFinancingMode) +
    new EnumNameSerializer(OrderPositionFill) +
    new EnumNameSerializer(OrderState) +
    new EnumNameSerializer(OrderTriggerCondition) +
    new EnumNameSerializer(OrderType) +
    new EnumNameSerializer(TimeInForce) +
    new EnumNameSerializer(PriceStatus) +
    new EnumNameSerializer(InstrumentType) +
    new EnumNameSerializer(TradeState) +
    new EnumNameSerializer(FundingReason) +
    new EnumNameSerializer(LimitOrderReason) +
    new EnumNameSerializer(MarketIfTouchedOrderReason) +
    new EnumNameSerializer(MarketOrderMarginCloseoutReason) +
    new EnumNameSerializer(MarketOrderReason) +
    new EnumNameSerializer(OrderCancelReason) +
    new EnumNameSerializer(OrderFillReason) +
    new EnumNameSerializer(StopLossOrderReason) +
    new EnumNameSerializer(StopOrderReason) +
    new EnumNameSerializer(TakeProfitOrderReason) +
    new EnumNameSerializer(TrailingStopLossOrderReason) +
    new EnumNameSerializer(TransactionRejectReason) +
    new EnumNameSerializer(TransactionFilter)
}
