package scala.spores.spark.run.transitive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores.spark.TestUtil._
import scala.spores.util.PluginFeedback._

@RunWith(classOf[JUnit4])
class TransitiveSerializableNegSpec {
  @Test
  def `Depth 1: Non serializable fields in classes are detected`(): Unit = {
    expectError(
      NonSerializableType("Foo", "value bar", "Bar")
    ) {
      """
        |import scala.spores._
        |class Bar
        |class Foo(val bar: Bar) extends Serializable
        |val foo = new Foo(new Bar)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Depth 2: Non serializable fields in classes are detected`(): Unit = {
    expectError(
      NonSerializableType("Bar", "value baz", "Baz")
    ) {
      """
        |import scala.spores._
        |abstract class Baz
        |class Bar(val baz: Baz) extends Serializable
        |class Foo(val bar: Bar) extends Serializable
        |val foo = new Foo(new Bar(new Baz {}))
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Depth 3: Non serializable fields in classes are detected`(): Unit = {
    expectError(
      NonSerializableType("Baz", "value baz2", "Baz2")
    ) {
      """
        |import scala.spores._
        |abstract class Baz2
        |abstract class Baz(val baz2: Baz2) extends Serializable
        |class Bar(val baz: Baz) extends Serializable
        |class Foo(val bar: Bar) extends Serializable
        |val foo = new Foo(new Bar(new Baz(new Baz2 {}) {}))
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect a wrapper around a function`(): Unit = {
    expectError(
      NonSerializableType("FakeWrapper", "value wrapped", "Int => Int")
    ) {
      """
        |import scala.spores._
        |class FakeWrapper(wrapped: Int => Int) extends Serializable
        |val foo = new FakeWrapper(i => i)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect warning in type params not annotated with Serializable`(): Unit = {
    expectWarning(
      NonSerializableTypeParam("UnserializableTypeParam", "T")
    ) {
      """
        |import scala.spores._
        |class Foo
        |class UnserializableTypeParam[T](typedValue: T) extends Serializable
        |val foo = new UnserializableTypeParam(new Foo)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect error in non-serializable tparams if force-serializable-type-params`(): Unit = {
    expectError(
      NonSerializableTypeParam("UnserializableTypeParam", "T"),
      "-P:spores-transitive-plugin:force-serializable-type-parameters"
    ) {
      """
        |import scala.spores._
        |class Foo
        |class UnserializableTypeParam[T](typedValue: T) extends Serializable
        |val foo = new UnserializableTypeParam(new Foo)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect warning in type params annotated with Serializable`(): Unit = {
    expectWarning(
      StoppedTransitiveInspection("SerializableTypeParam", "T", Some("SerializableTypeParam[Foo]"))
    ) {
      """
        |import scala.spores._
        |class Foo extends Serializable
        |class SerializableTypeParam[T <: Serializable](typedValue: T) extends Serializable
        |val foo = new SerializableTypeParam(new Foo)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect error in serializable tparams if force-transitive option is set`(): Unit = {
    expectError(
      StoppedTransitiveInspection("SerializableTypeParam", "T", Some("SerializableTypeParam[Foo]")),
      "-P:spores-transitive-plugin:force-transitive"
    ) {
      """
        |import scala.spores._
        |class Foo extends Serializable
        |class SerializableTypeParam[T <: Serializable](typedValue: T) extends Serializable
        |val foo = new SerializableTypeParam(new Foo)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  @Test
  def `Detect non-serializable subclasses of sealed serializable subclass`(): Unit = {
    expectError(
      NonSerializableType("Trap", "value o", "Object")
    ) {
      """
        |import scala.spores._
        |sealed class Foo extends Serializable
        |final case class Trap(o: Object) extends Foo
        |class SerializableTypeParam[T <: Serializable](typedValue: T) extends Serializable
        |val foo = new SerializableTypeParam(new Foo)
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }
}
