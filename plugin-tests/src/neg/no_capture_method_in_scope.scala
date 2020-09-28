import scala.spores._

class Data {
  def m(str: String) = println(str)
  val str = "This string is a global value."
  val s = spore {(x: Int) =>
    m(str)
    x
  }
}