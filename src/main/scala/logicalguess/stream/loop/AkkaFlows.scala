package logicalguess.stream.loop

import akka.NotUsed
import akka.stream.{ClosedShape, FlowShape, SourceShape}
import akka.stream.impl.fusing.TakeWhile
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, RunnableGraph, Sink, Source}

/**
  * Created by logicalguess on 12/11/16.
  */

object AkkaFlows {

  def samplerSource[R <: Loop[R]](init: R): Source[R, NotUsed] = Source.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val start = Source.single(init)
      val next = builder.add(Flow[R].map(_.next()))

      start ~> next

      SourceShape(next.out)
  })

  def loopSource[R <: Loop[R]](init: R): Source[R, NotUsed] = Source.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val start = Source.single(init)

      val next = Flow[R]
        .map(_.next())

      val bcast = builder.add(Broadcast[R](2))
      val merge = builder.add(MergePreferred[R](1))


      start ~> merge ~> bcast //~> next ~> merge.preferred
      merge.preferred <~ next <~ bcast

      SourceShape(bcast.out(1))
  })

  def loopFlow[R <: Loop[R]](stopCondition: R => Boolean, outputAll: Boolean = false): Flow[R, R, NotUsed] =
    Flow.fromGraph(GraphDSL.create() {
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val next = Flow[R]
          .map(_.next())

        val bcast = builder.add(Broadcast[R](2))
        val merge = builder.add(MergePreferred[R](1))
        val check = builder.add(TakeWhile(stopCondition))

        if (outputAll) {
          merge ~> bcast ~> check
          merge.preferred <~ next <~ bcast

          FlowShape(merge.in(0), check.out)

        } else {
          val last = builder.add(Flow[R].fold(null.asInstanceOf[R]) { (acc, value) => value })
          merge ~>                   bcast ~> check ~> last
          merge.preferred <~ next <~ bcast

          FlowShape(merge.in(0), last.out)
        }
    })

  def loopSource[R <: Loop[R]](init: R, stopCondition: R => Boolean) =
    Source.fromGraph(GraphDSL.create() { implicit builder =>

      val source = Source.single(init)
      val loop = builder.add(AkkaFlows.loopFlow[R](stopCondition))

      import GraphDSL.Implicits._

      source ~> loop

      SourceShape(loop.out)
    })

  def loopGraph[R <: Loop[R]](init: R, stopCondition: R => Boolean) =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>

      val source = Source.single(init)
      val loop = builder.add(AkkaFlows.loopFlow[R](stopCondition))
      val printFlow = builder.add(Flow[R].map {
        println(_)
      })
      val sink = builder.add(Sink.ignore)

      import GraphDSL.Implicits._

      source ~> loop ~> printFlow ~> sink

      ClosedShape
    })

}
