package scala.spores.spark.run.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class TransientSerializableSpec {
  @Test
  def `Depth 1: Ignore Scala transient fields in classes`(): Unit = {
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
  def `Depth 2: Ignore Scala transient fields in classes`(): Unit = {
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
  def `Depth 3: Ignore Scala transient fields in classes`(): Unit = {
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

  @Test
  def `Depth 1: Ignore Scala transient fields in traits`(): Unit = {
    trait Bar extends Serializable { val b: Function[Int, Int] }
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar { @transient val b = (i: Int) => i})
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Ignore Scala transient fields in traits`(): Unit = {
    trait Baz extends Serializable { val b: Function[Int, Int] }
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz { @transient val b = (i: Int) => i}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Ignore Scala transient fields in traits`(): Unit = {
    trait Baz2 extends Serializable { val b: Function[Int, Int] }
    abstract class Baz(baz2: Baz2) extends Serializable
    class Bar(val baz: Baz) extends Serializable
    class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2 { @transient val b = (i: Int) => i}) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Ignore non-transient accessor in trait and use transient field in class`(): Unit = {
    trait Base extends Serializable { val b: Function[Int, Int] }
    class Foo(@transient val b: Function[Int, Int]) extends Base
    val foo = new Foo(i => i)
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Correctly check recursive type`(): Unit = {
    // Not a real implementation, just illustrates use of recursive types
    trait HList[T] extends Serializable
    case class HCons[T <: HList[_]](value: T) extends HList[T]
    case object HNil extends HList[Int]
    val foo = HCons(HCons(HNil))
    spore {
      val captured = foo
      () =>
        captured
    }
  }
}
