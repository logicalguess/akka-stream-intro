package logicalguess.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.{Done, NotUsed}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.StdIn

object Linear {

  val source: Source[Int, _] = Source(1 to 10)
  val flow1: Flow[Int, Int, _] = Flow[Int].filter(_ % 3 == 0)
  val flow2: Flow[Int, Int, _] = Flow[Int].map(_ * 2)
  val sink: Sink[Int, Future[Done]] = Sink.foreach(println)

  val g: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    source ~> flow1 ~> flow2 ~> sink
    ClosedShape
  })

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("akka-stream-intro")
    implicit val materializer = ActorMaterializer()

    g.run()

    StdIn.readLine()

    val whenTerminated = system.terminate()
    Await.result(whenTerminated, Duration.Inf)

  }

}
