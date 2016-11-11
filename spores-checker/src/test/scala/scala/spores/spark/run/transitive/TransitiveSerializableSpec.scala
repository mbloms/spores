package scala.spores.spark.run.transitive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class TransitiveSerializableSpec {
  @Test
  def `Depth 1: Serializable fields in classes are detected`(): Unit = {
    class Bar extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar)
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Serializable fields in classes are ignored`(): Unit = {
    abstract class Baz extends Serializable
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Serializable fields in classes are ignored`(): Unit = {
    abstract class Baz2 extends Serializable
    abstract class Baz(val baz2: Baz2) extends Serializable
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2 {}) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }
}
