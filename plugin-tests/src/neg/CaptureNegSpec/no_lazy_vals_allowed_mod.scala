import scala.spores._

val s: Spore[Int, Unit] = spore {
  lazy val v1 = 10
  (x: Int) => println("arg: " + x + ", c1: " + v1)
}
