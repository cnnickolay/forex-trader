package org.nikosoft.oanda.bot.streaming

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._

import scala.concurrent.duration.DurationInt

object AkkaGraphTest extends App {

  implicit val actorSystem = ActorSystem("streamer")
  implicit val materializer = ActorMaterializer()

  val ticks: Source[String, Cancellable] = Source.tick(0.seconds, 1.seconds, ()).map(_ => "tick")
  val values: Source[Int, NotUsed] = Source(Stream.from(1).map(_ * 5))

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(Zip[String, Int]())

    ticks ~> zip.in0
    values ~> zip.in1

    zip.out ~> Sink.foreach(println)

    ClosedShape
  }).run()

}
