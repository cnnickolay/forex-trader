package org.nikosoft.oanda.bot.ml

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import scalaz.Scalaz._

class BalancedZip[T](nextEnrichCondition: (T, T) => Boolean) extends GraphStage[FanInShape2[T, T, (T, T)]] {

  private val in0 = Inlet[T]("BalancedZip.in0")
  private val in1 = Inlet[T]("BalancedZip.in1")
  private val out = Outlet[(T, T)]("BalancedZip.out")

  override def shape: FanInShape2[T, T, (T, T)] = new FanInShape2[T, T, (T, T)](in0, in1, out)

  def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var lastEnrichOption: Option[T] = None
    var nextEnrichOption: Option[T] = None
    var lastIncomingOption: Option[T] = None

    setHandler(in0, new InHandler {
      def onPush(): Unit = {
        lastIncomingOption = Option(grab(in0))
        ^(lastIncomingOption, lastEnrichOption) {(incoming, lastEnrich) =>
          nextEnrichOption match {
            case Some(nextEnrich) =>
              lastEnrichOption = nextEnrichOption
              nextEnrichOption = None
              nextEnrichCondition(incoming, nextEnrich)
            case None =>
          }
          push(out, (incoming, lastEnrich))
        }
      }
    })

    setHandler(in1, new InHandler {
      def onPush(): Unit = {
        val grabbedValue = Option(grab(in1))
        (lastEnrichOption, nextEnrichOption) match {
          case (Some(_), Some(_)) =>
            lastEnrichOption = nextEnrichOption
            nextEnrichOption = grabbedValue
          case (Some(_), None) => nextEnrichOption = grabbedValue
          case (None, None) => lastEnrichOption = grabbedValue
        }
      }
    })

    setHandler(out, new OutHandler {
      def onPull(): Unit = {
        if (nextEnrichOption.isEmpty) pull(in1)
        pull(in0)
      }
    })
  }
}

object Main extends App {

  implicit val actorSystem = ActorSystem("test")
  implicit val actorMaterializer = ActorMaterializer()

  val source1 = Source(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  val source2 = Source(List(1, 5, 7, 10))

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(new BalancedZip[Int]((main, enrich) => main > enrich))

    source1 ~> zip.in0
    source2 ~> zip.in1
               zip.out ~> Sink.foreach[(Int, Int)](println)

    ClosedShape
  }).run()

}