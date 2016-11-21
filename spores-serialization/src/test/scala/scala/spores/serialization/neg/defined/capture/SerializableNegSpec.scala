package scala.spores.serialization.neg.defined.capture

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores.serialization.TestUtil._
import scala.spores.util.PluginFeedback.nonSerializableType

@RunWith(classOf[JUnit4])
class SerializableNegSpec {
  @Test
  def `A class is not serializable`() {
    expectError(nonSerializableType("anonspore$macro$1", "value captured", "Foo")) {
      """
        import scala.spores._
        class Foo(val number: Int)
        val foo = new Foo(1)
        spore {
          val captured = foo
          () => captured
        }
      """
    }
  }

@Test
  def `A trait is not serializable`() {
    expectError(nonSerializableType("anonspore$macro$1", "value captured", "Foo")) {
      """
        import scala.spores._
        trait Foo { val number: Int }
        val foo = new Foo { val number = 1 }
        spore {
          val captured = foo
          () => captured
        }
      """
    }
  }

  @Test
  def `An abstract class is not serializable`() {
    expectError(nonSerializableType("anonspore$macro$1", "value captured", "Foo")) {
      """
        import scala.spores._
        abstract class Foo(val number: Int)
        val foo = new Foo(1) {}
        spore {
          val captured = foo
          () => captured
        }
      """
    }
  }

  @Test
  def `An object is not serializable`() {
    expectError(nonSerializableType("anonspore$macro$1", "value captured", "Foo")) {
      """
        import scala.spores._
        object Foo { val number = 1 }
        val foo = Foo
        spore {
          val captured = foo
          () => captured
        }
      """
    }
  }
}
