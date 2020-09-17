import scala.spores._

object CapturedTest {
  def main(args: Array[String]): Unit = {
    val hejdu: String = "hejdu"
    // Note that the type declaration isn't necessary
    // Inferred type: NullarySporeWithEnv[Unit]{type Captured = hejdu.type}
    val s /* : NullarySporeWithEnv[Unit] {type Captured <: String}*/ =
    spore {
      val str = hejdu;
      val num = 2;
      {() =>
        println(str)
        println(num)
      }
    }
    s.apply()
    //It even infers this:
    //val c: hejdu.type = s.captured
    //println(c)
  }
}
