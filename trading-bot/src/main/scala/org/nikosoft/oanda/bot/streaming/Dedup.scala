package org.nikosoft.oanda.bot.streaming

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape}
import akka.stream.scaladsl._

object Dedup extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val source = Source(List(1, 1, 1, 2, 2, 2, 2, 2, 3, 4, 4, 5, 6))

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val index = Source(Stream.from(1))
    val zip = builder.add(Zip[Int, Int]())
    index ~> zip.in0
    source ~> zip.in1

    zip.out ~> Flow[(Int, Int)]
      .sliding(2, 1)
      .mapConcat { case (prevIdx, prevValue) +: (idx, value) +: _ =>
        if (idx == 1) List(prevValue, value)
        else if (prevValue == value) Nil
        else List(prevValue)
      } ~> Sink.foreach(println)

    ClosedShape
  }).run()

}
