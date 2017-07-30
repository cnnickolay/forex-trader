package org.nikosoft.oanda.bot

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, ActorRef}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.nikosoft.oanda.api.Api
import org.nikosoft.oanda.api.ApiModel.InstrumentModel.{Candlestick, CandlestickData}
import org.nikosoft.oanda.api.ApiModel.PrimitivesModel.{DateTime, InstrumentName}
import org.nikosoft.oanda.bot.CommonCommands.Tick
import org.nikosoft.oanda.instruments.Model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationLong
import scalaz.{-\/, \/-}

class CandleStreamingTesterActor(next: ActorRef, chart: Chart) extends Actor {

  implicit val flowMaterializer = ActorMaterializer()

  override def preStart() = self ! Tick

  def receive = {
    case Tick =>
      val startFrom = LocalDateTime.parse("2016-06-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME)

      val input = Source((0 to 5 by 5).map(startFrom.plusDays(_)))
      val flow = Flow[LocalDateTime]
        .map(from => {
          val to = from.plusDays(5)
          Api.instrumentsApi
            .candles(
              instrument = InstrumentName(chart.instrument),
              granularity = chart.granularity,
              from = Some(DateTime(from.format(DateTimeFormatter.ISO_DATE_TIME) + "Z")),
              to = Some(DateTime(to.format(DateTimeFormatter.ISO_DATE_TIME) + "Z"))
            )
        })
        .map {
          case -\/(err) => println(err); Nil
          case \/-(response) => response.candles
        }
        .filter(_.nonEmpty)
        .map(_.flatMap(toCandleStick))
        .map(candles => candles.flatMap(chart.addCandleStick).reverse)

      val sink = Sink.foreach[Seq[CandleStick]](candles => next ! candles)

      input.via(flow).runWith(sink)
  }

  def toCandleStick(candle: Candlestick): Option[CandleStick] = candle.mid.map(c => CandleStick(candle.time.toInstant, c.o.value, c.h.value, c.l.value, c.c.value, candle.volume, candle.complete))
}
