import scala.spores._

object CapturedTest {
  def main(args: Array[String]): Unit = {
    val hejdu: String = "hejdu"
    val s =
    spore {
      val str = hejdu;
      val num = 2;
      {() =>
        println(str)
        println(num)
      }
    }
    s.apply()
  }
}
