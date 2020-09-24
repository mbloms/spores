import scala.spores._

object Main {
  def main: Spore[Null,Unit] =
    spore {ingenting =>
      println("hejdu")
    }
  def bad[A,B](fun: (A => B)): Spore[A,B] = spore(fun)
}