package logicalguess.stream.stage

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.io.StdIn

/**
  * Created by logicalguess on 12/11/16.
  */
object Stages {

  val filterStage: Graph[FlowShape[Int, Int], NotUsed] = new GraphStage[FlowShape[Int, Int]] {
    val in = Inlet[Int]("in")
    val out = Outlet[Int]("out")
    override val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        // All state MUST be inside the GraphStageLogic

        def filter(elem: Int) : Boolean = {
          elem match  {
            case e if( e % 2 == 0 ) => true
            case _ => false
          }
        }
        setHandler(in, handler = new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            if (filter(elem)) push(out, elem)
            else pull(in)
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }
  }

  val counterStage: Graph[FlowShape[Int, Int], NotUsed] = new GraphStage[FlowShape[Int, Int]] {
    val in = Inlet[Int]("in")
    val out = Outlet[Int]("out")
    override val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        private var counter = 1

        setHandler(in, handler = new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            counter += 1
            push(out, elem)
          }
          override def onUpstreamFinish(): Unit = {
            emit(out, -counter)
            complete(out)
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }
  }

  val timedGate: Graph[FlowShape[Int, Int], NotUsed] = new TimedGate[Int](1 second)


  val p = Promise[Unit]
  val f = p.future
  val killSwitch: Graph[FlowShape[Int, Int], NotUsed] = new KillSwitch[Int](f)



  def main(args: Array[String]): Unit = {
    val sink: Sink[Int, Future[Done]] = Sink.foreach(println)

    val graphs = List(filterStage, counterStage, timedGate, killSwitch)
    val sources = List(Source(11 to 19), Source(11 to 19), Source(1 to 100000000), Source(1 to 100000000))
    val names = List("Filter", "Counter", "Timed Gate", "Kill Switch")

    println("")
    println("***************************")
    println("Choose a Stage:")
    println("***************************")
    println("")
    Iterator.range(0, graphs.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < graphs.size) => {
        val g = graphs(idx)
        val source = sources(idx)

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()
        implicit val ec = system.dispatcher

        source.via(g).toMat(sink)(Keep.right).run()

        system.scheduler.scheduleOnce(500 millis) {p success()}

        StdIn.readLine()
        system.terminate()
      }
      case _ =>
    }
  }


}
