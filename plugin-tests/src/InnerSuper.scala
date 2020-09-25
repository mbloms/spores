import scala.spores._

object Outer {
  val l = 1
  def main(args: Array[String]): Unit = {
    val s = spore {
      val cl = l
      {() =>
        class Inner {
          def inner = super.toString
        }
        val inner = new Inner
        inner.inner
      }
    }
  }
}