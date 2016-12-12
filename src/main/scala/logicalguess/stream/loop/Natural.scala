package logicalguess.stream.loop

/**
  * Created by logicalguess on 12/11/16.
  */
case class Natural(value: Int) extends Loop[Natural] {
  def next() = Natural(value + 1)
}
