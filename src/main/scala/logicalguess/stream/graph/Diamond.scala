package logicalguess.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, SourceShape}

import scala.io.StdIn

object Diamond {

  val convert = Flow[Int].map(_.toString)
  val bang = Flow[String].map(_ + "!")
  val hash = Flow[String].map(_ + "#")
  val concatenate = Flow[(String, String)].map(p => p._1 + p._2)

  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val in = Source(1 to 10)
    val out = Sink.foreach(println)
    val bcast = builder.add(Broadcast[String](2))
    val zip = builder.add(Zip[String, String])

    in ~> convert ~> bcast ~> bang ~> zip.in0
                     bcast ~> hash ~> zip.in1
                                      zip.out ~> concatenate ~> out
    ClosedShape
  })

  val source = Source.fromGraph( GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val merge = builder.add(Merge[Int](2))
    Source.single(0)      ~> merge
    Source(List(2, 3, 4)) ~> merge

    SourceShape(merge.out)
  })

  val diamond = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val input = builder.add(Flow[Int])
    val bcast = builder.add(Broadcast[String](2))
    val zip = builder.add(Zip[String, String])
    val output = builder.add(Flow[String])

    input.out ~> convert ~> bcast ~> bang ~> zip.in0
                            bcast ~> hash ~> zip.in1
                                             zip.out ~> concatenate ~> output

    FlowShape(input.in, output.out)
  })

  val sourceAndFlow = source.via(diamond).to(Sink.foreach(println))

  def main(args: Array[String]): Unit = {

    val graphs = List(graph, sourceAndFlow)
    val names = List("ClosedShape", "SourceShape and FlowShape")

    println("")
    println("***************************")
    println("Choose a Shape:")
    println("***************************")
    println("")
    Iterator.range(0, graphs.size).foreach {
      case idx => println(s"$idx:  ${names(idx)}")
    }
    println("")

    StdIn.readInt() match {
      case idx if (idx >= 0 && idx < graphs.size) => {
        val g = graphs(idx)

        implicit val system = ActorSystem("akka-stream-intro")
        implicit val materializer = ActorMaterializer()

        g.run()

        StdIn.readLine()
        system.terminate()
      }
      case _ =>
    }
  }
}
