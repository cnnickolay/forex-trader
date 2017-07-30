package org.nikosoft.oanda.bot.streaming

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}

import scala.concurrent.duration.DurationInt

object Dedup extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val source = Source(List(1, 2, 2, 2, 2, 2, 3, 4, 4, 5, 6))

  val flow = Flow[Int]
    .sliding(2, 1)
    .mapConcat { case prev +: next +: _ => if (prev == next) Nil else List(next) }


  source
    .via(flow)
    .runWith(Sink.foreach(println))


}
