package logicalguess.stream.loop

/**
  * Created by logicalguess on 12/3/16.
  */

trait Loop[Self <: Loop[Self]] {
  def next(): Self
}
