import scala.spores._

class Data {
  def id(x: Int) = x
  val s = spore {(x: Int) => println(s"${x} blah ${id(x)}")}
}