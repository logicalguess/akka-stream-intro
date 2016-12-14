package logicalguess.stream.loop

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

/**
  * Created by logicalguess on 12/11/16.
  */
object App {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("akka-stream-intro")
    implicit val materializer = ActorMaterializer()

    //        AkkaFlows.samplerSource[Fibonacci](Fibonacci(1, 1))
    //          .runForeach(println)
    //          .onComplete(_ => system.terminate())

    //    AkkaFlows.loopSource[Fibonacci](Fibonacci(1, 1))
    //      .takeWhile(_.value < 500)
    //      .runForeach(println)
    //      .onComplete(_ => system.terminate())

    //    AkkaFlows.loopSource[Natural](Natural(0))
    //      .takeWhile(_.value < 50)
    //      .runForeach(println)
    //      .onComplete(_ => system.terminate())

//    Source.single(Natural(0))
//      .via(AkkaFlows.loopFlow[Natural](n => n.value < 15, false))
//      .runForeach(println)
//      .onComplete(_ => system.terminate())

//    Source.single(Natural(0, n => n.value < 15))
//      .via(AkkaFlows.loop[Natural]())
//      .runForeach(println)
//      .onComplete(_ => system.terminate())

//    val nats: PartialFunction[Int, Int] = {
//      case i: Int if (i >= 0 && i < 15) => i + 1
//    }
//
//    Source.single(0)
//      .via(AkkaFlows.iterate(nats))
//      .runForeach(println)
//      .onComplete(_ => system.terminate())

    val fibs: PartialFunction[(Int, Int), (Int, Int)] = {
      case p: (Int, Int) if (p._1 >= 0) => (p._1 + p._2, p._1)
    }

    val restrict: PartialFunction[(Int, Int), (Int, Int)] = {
      case p: (Int, Int) if (p._1 < 15) => p

    }

    Source.single((1, 1))
      .via(AkkaFlows.iterate(restrict.andThen(fibs), true))
      .map(_._1)
      .runForeach(println)
      .onComplete(_ => system.terminate())

//    Source.repeat(1)
//      .scan(Natural(0)) { (acc, _) => acc.next() }
//      .takeWhile(_.value < 50)
//      .runForeach(println)
//      .onComplete(_ => system.terminate())

    //    val result = Source.single(Natural(0))
    //      .via(AkkaFlows.loopFlow[Natural](n => n.value < 15))
    //      .runWith(Sink.fold(null.asInstanceOf[Natural]) { (acc, value) => value })
    //      .value
    //    println(result)


    //        AkkaFlows.loopSource[Natural](Natural(0), (n: Natural) => n.value < 25)
    //          .runForeach(println)
    //          .onComplete(_ => system.terminate())

    //AkkaFlows.loopGraph[Natural](Natural(0), n => n.value < 10).run()

  }
}
