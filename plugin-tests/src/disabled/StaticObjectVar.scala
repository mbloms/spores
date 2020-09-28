import scala.spores._

/** It's not obvious if this should be allowed. */

object Data {
  def m(str: String) = println(str)
  var str = "This string is a global value."
  val s = spore {(x: Int) =>
    str = str + " " + x
    m(str)
  }
}