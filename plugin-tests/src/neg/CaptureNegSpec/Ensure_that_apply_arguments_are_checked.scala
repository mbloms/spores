import scala.spores._

object TrapUtil {
  def defWithSeveralArgs(xs: String*) = xs.foreach(println)
}
class Main:
  val trap = "I am a trap"
  def main = spore {
    () => TrapUtil.defWithSeveralArgs("Hello", "World", trap, "END.")
  }