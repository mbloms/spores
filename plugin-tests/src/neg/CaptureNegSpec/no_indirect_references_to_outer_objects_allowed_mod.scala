import scala.spores._

class C {
  def m(i: Int): Any = "example " + i
}

object TopLevelObject {
  val f = new C
}

class Stuff:
  val outerObject = TopLevelObject.f
  val s = spore {
    (x: Int) =>
      val s1 = outerObject.m(x).asInstanceOf[String]
      s1 + "!"
  }
