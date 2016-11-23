import scala.spores._

object HelloSpores extends App {
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
}
