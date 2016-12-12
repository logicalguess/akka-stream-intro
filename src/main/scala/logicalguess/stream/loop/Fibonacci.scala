package logicalguess.stream.loop

/**
  * Created by logicalguess on 12/11/16.
  */
case class Fibonacci(value: Int, previous: Int) extends Loop[Fibonacci] {
  def next() = Fibonacci(value + previous, value)
}
