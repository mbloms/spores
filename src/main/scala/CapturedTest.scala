import scala.spores._

object CapturedTest {
  val hejdu: String = "hejdu"
  def main(args: Array[String]): Unit = {
    val s =
    spore {
      lazy val str = hejdu;
      val num = 2;
      {() =>
        println(str)
        println(num)
      }
    }
    s.apply()
  }
}
