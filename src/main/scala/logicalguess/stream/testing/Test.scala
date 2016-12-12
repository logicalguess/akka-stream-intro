package logicalguess.stream.testing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import logicalguess.stream.graph.Diamond

import scala.io.StdIn

/**
  * Created by logicalguess on 12/11/16.
  */
object Test {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("akka-stream-intro")
    implicit val materializer = ActorMaterializer()

    val (pub, sub) = TestSource.probe[Int]
      .via(Diamond.diamond)
      .toMat(TestSink.probe[Any])(Keep.both)
      .run()

    sub.request(1)
    pub.sendNext(7)
    sub.expectNext("7!7#")

    println("All good!")

    StdIn.readLine()
    system.terminate()
  }

}
