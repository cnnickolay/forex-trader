package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

object Dedup extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val source = Source(List(1, 1, 1, 2, 2, 2, 2, 2, 3, 4, 4, 5, 6))

  val flow = Flow[Int]
    .sliding(2, 1)
    .mapConcat { case prev +: current +: _=>
        if (prev == current) Nil
        else List(current)
    }

  // this value should be prepended to the source in order to avoid losing the first value
  val ignoredValue = Int.MinValue
  val prependedSource = Source.single(ignoredValue).concat(source)
  prependedSource.via(flow).runWith(Sink.foreach(println))
}
