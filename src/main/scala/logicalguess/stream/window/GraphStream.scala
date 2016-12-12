package logicalguess.stream.window

import akka.actor.{ActorSystem, Props}
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import logicalguess.stream.actor.SinkActor
import logicalguess.stream.domain.{Category, Event, RandomEvent}

import scala.concurrent.duration._
import scala.io.StdIn

object GraphStream {

  type Transformer[In, Out] = Graph[FlowShape[In, Out], _]

  case object Tick
  val events: Source[Event, _] = Source.tick(0 millis, 100 millis, Tick).map(_ => RandomEvent())

  def countsByCategoryMap = {

    val processor: Transformer[Event, Map[String, Int]] = Flow[Event]
      .groupedWithin(Int.MaxValue, 1.second)
      .map(w => w.groupBy(_.category).map { case (category, events) => category.toString -> events.length })

    val g = RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        // Source
        val A: Outlet[Event] = builder.add(events).out

        // Flows
        val B: FlowShape[Event, Map[String, Int]] = builder.add(processor)

        // Sinks
        val C: Inlet[Any] = builder.add(Sink.foreach(println)).in

        A ~> B ~> C

        ClosedShape
    })
    g
  }

  def countsByCategoryFilter = {

    def processor(category: String): Transformer[Event, (String, Int)] = Flow[Event]
      .filter(_.category.toString == category)
      .groupedWithin(Int.MaxValue, 1.second)
      .map[(String, Int)](es => (category, es.length))

    val g = RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        // Source
        val A: Outlet[Event] = builder.add(events).out

        // Flows
        val B: UniformFanOutShape[Event, Event] = builder.add(Broadcast[Event](3))

        val C: FlowShape[Event, (String, Int)] = builder.add(processor(Category.info.toString))
        val D: FlowShape[Event, (String, Int)] = builder.add(processor(Category.warning.toString))
        val E: FlowShape[Event, (String, Int)] = builder.add(processor(Category.error.toString))

        val F: UniformFanInShape[(String, Int), (String, Int)] = builder.add(Merge[(String, Int)](3))

        // Sinks
        //val G: Inlet[Any] = builder.add(Sink.foreach(println)).in
        val G: SinkShape[Any] = builder.add(Sink.actorSubscriber(Props(classOf[SinkActor], "sinkActor")))

        A ~> B ~> C ~> F ~> G
             B ~> D ~> F
             B ~> E ~> F

        ClosedShape
    })
    g
  }

  def main(args: Array[String]): Unit = {

    val graphs = List(countsByCategoryMap, countsByCategoryFilter)
    val names = List("Counts By Category Map", "Counts By Category Filter")

    println("")
    println("***************************")
    println("Choose a scenario:")
    println("***************************")
    println("")
    Iterator.range(0, graphs.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < graphs.size) => {
        val graph = graphs(idx)

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()

        graph.run()

        StdIn.readLine()
        system.terminate()
      }
      case _ =>
    }
  }

}
