import scala.spores._

object CapturedTest {
  def main(args: Array[String]): Unit = {
    val hejdu = "hejdu"
    val s: NullarySporeWithEnv[Unit] =
      spore {
        val str = hejdu;
        {() => println(str)}
      }
    s.apply()
    val c: String = s.captured.asInstanceOf[String]
    println(c)
  }
}
