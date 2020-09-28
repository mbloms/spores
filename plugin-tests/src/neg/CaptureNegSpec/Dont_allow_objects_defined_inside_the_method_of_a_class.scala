import scala.spores._

class Base { val f = 1 }

class Outer {
  def method(): Unit = {
    object Inner extends Base {
      val s = spore { (x: Int) =>
        f.toString + x.toString + "!"
      }
    }
  }
}
