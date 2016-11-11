package scala.spores.spark.run.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class TransientSerializableSpec {
  @Test
  def `Depth 1: Ignore Scala transient fields`(): Unit = {
    class Bar(@transient val b: Function[Int, Int]) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(i => i))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Ignore Scala transient fields`(): Unit = {
    abstract class Baz(@transient val a: Object) extends Serializable
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Object) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Ignore Scala transient fields`(): Unit = {
    class Baz2(@transient val a: Object) extends Serializable
    abstract class Baz(val baz2: Baz2) extends Serializable
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2(new Object)) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }
}
