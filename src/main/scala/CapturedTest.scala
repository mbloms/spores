import scala.spores._

object CapturedTest {
  def main(args: Array[String]): Unit = {
    val hejdu: String = "hejdu"
    val s: NullarySporeWithEnv[Unit] {type Captured <: String} =
      spore {
        val str = hejdu;
        {() => println(str)}
      }
    s.apply()
    val c: String = s.captured
    println(c)
  }
}
