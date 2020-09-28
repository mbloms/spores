import scala.spores._

/** There is no good reason why this should not be allowed if StaticObject is ok */

class SuperData {
  def m(str: String) = println(str)
}

object Data extends SuperData {
  var str = "This string is a global value."
  val s = spore {(x: Int) =>
    this.str = s"${this.str} (${x})"
    this.m(str)
  }
}
