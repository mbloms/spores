import scala.spores._

class TrapUtil {
  def defWithSeveralArgs(xs: String*) = xs.foreach(println)
  def hehe(a: String) = {
    val trap = "I am a trap"
    spore {
      () => defWithSeveralArgs("Hello", "World", trap, "END.")
    }
  }
}
       