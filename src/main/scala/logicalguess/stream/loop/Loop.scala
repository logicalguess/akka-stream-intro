package logicalguess.stream.loop

/**
  * Created by logicalguess on 12/3/16.
  */

trait Loop[Self <: Loop[Self]] {
  def next(): Self
}

trait Recursive[Self <: Recursive[Self]] extends PartialFunction[Self, Self] {
  def next() = apply(this.asInstanceOf[Self])
}

case class Condition[T](predicate: T => Boolean) extends PartialFunction[T, T] {
  override def isDefinedAt(t: T): Boolean = predicate(t)
  override def apply(t: T): T = t
}
