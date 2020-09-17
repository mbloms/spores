package spores

class Spore[-A,+B](fun: A => B) extends (A => B) {
  def apply(x: A): B = fun(x)
}

object Spore {
  def spore[A,B](fun: A => B): Spore[A, B] = new Spore[A, B](fun)
}

object Main {
  import Spore._
  def main: Spore[Null,Unit] =
    spore {ingenting =>
      println("hejdu")
    }
  def bad[A,B](fun: (A => B)): Spore[A,B] = spore(fun)
}