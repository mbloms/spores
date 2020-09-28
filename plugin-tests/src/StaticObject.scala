import scala.spores._

/** It's not obvious if this should be allowed. */

object Data {
  def m(str: String) = println(str)
  val str = "This string is a global value."
  val s = spore {(x: Int) =>
    m(str)
    x
  }
}