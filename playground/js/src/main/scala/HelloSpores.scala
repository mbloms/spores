import scala.scalajs.js
import scala.spores._

object HelloSpores extends js.JSApp {
  def main(): Unit = {
    val s = spore {
      val capturedInt = 8
      val capturedString = "Hello, World!"
      val capturedList = List(1, 2, 3, 4)
      (i: Int) =>
        {
          println(capturedString)
          capturedList.map(_ + i).contains(capturedInt)
        }
    }
    s(10)
  }
}
