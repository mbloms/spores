package spores

class Spore[-A,+B](fun: A => B) extends (A => B) {
  type Excluded

  def apply(x: A): B = fun(x)
}

object Spore {
  def spore[A,B,E](fun: A => B): Spore[A, B]{type Excluded = E} = new Spore[A, B](fun){type Excluded = E}
}

object Main {
  import Spore._
  def foo(s: Spore[Int,Unit]{type Excluded = Array[Int]}) = s(0)
  var arr: Array[Int] = new Array(10)
  def bad = foo(spore {(i: Int) =>
    arr(i) = 1
  })
}