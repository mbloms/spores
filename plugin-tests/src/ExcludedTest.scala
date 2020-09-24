import scala.spores._

object Main {
  def foo(s: NullarySpore[Unit]{type Excluded = Array[Int]}) = s()
  var arr: Array[Int] = new Array(10)
  def bad = foo(spore {
    val carr = arr
    {() =>
      carr(1) = 1
    }})
}