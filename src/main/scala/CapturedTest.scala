import scala.spores._

object CapturedTest {
  def main(args: Array[String]): Unit = {
    val (t,s: NullarySpore[Unit]) =
      spore {
        val str = "hejduu";
        {() => println(str)}
      }
    s.apply()
    println(t.toString)
  }
}
