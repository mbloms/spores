package scala.spores.serialization.run.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class TransientSerializableSpec {
  @Test
  def `Depth 1: Ignore Scala transient fields in classes`(): Unit = {
    final class Bar(@transient val b: Function[Int, Int]) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(i => i))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Ignore Scala transient fields in classes`(): Unit = {
    sealed abstract class Baz(@transient val a: Object) extends Serializable
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Object) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Ignore Scala transient fields in classes`(): Unit = {
    final class Baz2(@transient val a: Object) extends Serializable
    sealed abstract class Baz(val baz2: Baz2) extends Serializable
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2(new Object)) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 1: Ignore Scala transient fields in traits`(): Unit = {
    sealed trait Bar extends Serializable { val b: Function[Int, Int] }
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar { @transient val b = (i: Int) => i})
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Ignore Scala transient fields in traits`(): Unit = {
    sealed trait Baz extends Serializable { val b: Function[Int, Int] }
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz { @transient val b = (i: Int) => i}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Ignore Scala transient fields in traits`(): Unit = {
    sealed trait Baz2 extends Serializable { val b: Function[Int, Int] }
    sealed abstract class Baz(baz2: Baz2) extends Serializable
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2 { @transient val b = (i: Int) => i}) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Ignore non-transient accessor in trait and use transient field in class`(): Unit = {
    sealed trait Base extends Serializable { val b: Function[Int, Int] }
    final class Foo(@transient val b: Function[Int, Int]) extends Base
    val foo = new Foo(i => i)
    spore {
      val captured = foo
      () =>
        captured
    }
  }
}
