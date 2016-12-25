package logicalguess.stream.loop

/**
  * Created by logicalguess on 12/11/16.
  */
case class Natural(value: Int, filter: Natural => Boolean = _ => true) extends Recursive[Natural] {

  override def isDefinedAt(n: Natural): Boolean = n.value >= 0 && filter(n)
  override def apply(n: Natural): Natural = new Natural(n.value + 1, filter)

  override def toString(): String = value.toString
}
