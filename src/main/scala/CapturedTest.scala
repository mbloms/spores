import scala.spores._

object CapturedTest {
  case class Data[T](x: T)
  def main(args: Array[String]): Unit = {
    val d = Data("hejdu")
    val s = spore {
      val str = d.x
      val num = 2
      {() =>
        println(str)
        println(num)
      }
    }
    s.apply()
  }
}
