package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, SinkShape}
import akka.stream.scaladsl._

object Dedup extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val source = Source(List(1, 1, 1, 2, 2, 2, 2, 2, 3, 4, 4, 5, 6))

  val flow = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val index = Source(Stream.from(1))
    val zip = builder.add(Zip[Int, Int]())

    index ~> zip.in0

    val output = zip.out
      .sliding(2, 1)
      .mapConcat { case (idx, value) +: (prevIdx, prevValue) +: _ =>
        if (prevIdx == 1) List(value, prevValue)
        else if (value == prevValue) Nil
        else List(value)
      }

    FlowShape(zip.in1, output.outlet)
  })

  source.via(flow).runWith(Sink.foreach(println))
}
