package scala.spores.serialization.run.transitive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._
import scala.spores.hierarchy.{JavaBar, JavaFoo}

@RunWith(classOf[JUnit4])
class TransitiveSerializableSpec {
  @Test
  def `Depth 1: Serializable fields in classes are detected`(): Unit = {
    final class Bar extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar)
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 2: Serializable fields in classes are ignored`(): Unit = {
    sealed abstract class Baz extends Serializable
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Depth 3: Serializable fields in classes are ignored`(): Unit = {
    sealed abstract class Baz2 extends Serializable
    sealed abstract class Baz(val baz2: Baz2) extends Serializable
    final class Bar(val baz: Baz) extends Serializable
    final class Foo(val bar: Bar) extends Serializable
    val foo = new Foo(new Bar(new Baz(new Baz2 {}) {}))
    spore {
      val captured = foo
      () =>
        captured
    }
  }

  @Test
  def `Compile correctly type params annotated with Serializable, by default`(): Unit = {
    import scala.spores.PrimitiveSerializationWitnesses._
    final class SerializableTypeParam[T : CanSerialize](typedValue: T)
        extends Serializable
    val foo = new SerializableTypeParam(1)
    val s = spore {
      val captured = foo
      () =>
        captured
    }
    s()
  }

  @Test
  def `Compile correctly type params not annotated with Serializable, by default`(): Unit = {
    final class NoSerializableTypeParam[T](typedValue: T)
      extends Serializable
    val foo = new NoSerializableTypeParam[Int](1)
    val s = spore {
      val captured = foo
      () =>
        captured
    }
    s()
  }

  @Test
  def `Correctly check recursive type`(): Unit = {
    sealed trait HList extends Product with Serializable
    final case class ::[+H, +T <: HList](head : H, tail : T) extends HList
    case object HNil extends HList
    val foo = ::("", ::(1, HNil))
    spore {
      val captured = foo
      () => captured
    }
  }

  @Test
  def `Ignore type parameters that are bounded with closed hierarchies`(): Unit = {
    final class DamnHowSerializableIAm extends Serializable
    sealed trait Foo extends Serializable {val foo: String}
    final case class Bar(foo: String, bar: Int) extends Foo
    final case class Bar2(foo: String, bar2: Float) extends Foo
    final case class Baz(foo: String, damn: DamnHowSerializableIAm) extends Foo

    class Wrapper[T <: Foo](val wrapped: T) {
      spore {
        val captured = wrapped
        () => captured
      }
    }
    assert(new Wrapper(Bar(new String(""), 11111)).wrapped.bar == 11111)
  }

  @Test
  def `Ignore type parameters that are bounded with closed hierarchies in the presence of transitive parameters`(): Unit = {
    final class DamnHowSerializableIAm extends Serializable
    sealed trait Foo extends Serializable {val foo: String}
    sealed class Bar2(val foo: String, val bar2: Float) extends Foo
    final case class Baz(foo: String, damn: DamnHowSerializableIAm) extends Foo
    sealed trait Bar3 extends Foo { val hehe: String }
    final case class CatchMe(@transient a: Object, hehe: String, foo: String) extends Bar3

    class Wrapper[T <: Foo](val wrapped: T) {
      spore {
          val captured = wrapped
          () => captured
      }
    }
    assert(new Wrapper(CatchMe(new String(""), "Hello, World!", "")).wrapped.hehe == "Hello, World!")
  }

  @Test
  def `Allow user to assume closed hierarchies for java/scala binaries in type parameters`(): Unit = {
    class JavaWrapper[@assumeClosed T <: JavaFoo](val wrapped: T) {
      spore {
        val captured = wrapped
        () => captured
      }
    }
    assert(new JavaWrapper(new JavaBar("haha")).wrapped.getName == "haha")
  }

  @Test
  def `Allow user to assume closed hierarchies for java/scala binaries in captured variables`(): Unit = {
    val foo: JavaFoo = new JavaBar("Hello, World!")
    spore {
      val captured = foo: @assumeClosed
      () => captured
    }
  }

  @Test
  def `Allow user to assume closed hierarchies for java/scala binaries in type parameters of captured variables`(): Unit = {
    val foo: JavaFoo = new JavaBar("Hello, World!")
    sealed class Wrapper[T <: java.io.Serializable](foo: T) extends Serializable

    val wrapper = new Wrapper(foo: JavaFoo @assumeClosed)
    spore {
      val captured: Wrapper[JavaFoo @assumeClosed] = wrapper
      () => captured
    }
  }

  @Test
  def `Allow user to assume closed hierarchies for java/scala binaries in fields of captured variables`(): Unit = {
    val foo: JavaFoo = new JavaBar("Hello, World!")
    sealed class Wrapper(foo: JavaFoo @assumeClosed) extends Serializable

    val wrapper: Wrapper = new Wrapper(foo)
    spore {
      val captured = wrapper
      () => captured
    }
  }

  @Test
  def `Compile a concrete instantiated closed class hierarchy`(): Unit = {
    sealed trait Foo[T] extends Serializable {val foo: T}
    final case class Bar[T](foo: T) extends Foo[T]
    val foo = Bar("muahaha")
    spore {
      val captured = foo
      () => captured
    }
  }

  @Test
  def `Compile a HK closed class hierarchy whose subclass defines more type parameters`(): Unit = {
    sealed trait Foo[T] extends Serializable {val foo: T}
    final case class ExtraBar[T, U](foo: T, bar: U) extends Foo[T]
    val foo = ExtraBar("muahaha", new Integer(1))
    spore {
      val captured = foo
      () => captured
    }
  }


  @Test
  def `Compile a HK closed class hierarchy whose subclass defines more type parameters and applies them in a reverse way`(): Unit = {
    sealed trait Foo[T] extends Serializable
    final case class ReversedExtraBar[T, U](foo: T, bar: U) extends Foo[U]
    val foo = ReversedExtraBar("muahaha", new Integer(1))
    spore {
      val captured = foo
      () => captured
    }
  }
}
