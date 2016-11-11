package scala.spores.spark.neg.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores.spark.TestUtil._
import scala.spores.util.PluginFeedback.NonSerializableType

@RunWith(classOf[JUnit4])
class InteropJavaSerializableNegSpec {
  @Test
  def `Depth 2: Non serializable fields in classes are detected`(): Unit = {
    expectError(
      NonSerializableType("JavaTransientNonSerializableOwner", "variable member", "Object")
    ) {
      """
        |import scala.spores._
        |val interop = new JavaInteropEntrypoint()
        |val foo = interop.transientNonSerializable
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }

  def `Depth3: Non serializable fields in classes are detected`(): Unit = {
    expectError(
      NonSerializableType("JavaTransientNonSerializableOwner2", "variable member", "JavaTransientNonSerializableMember")
    ) {
      """
        |import scala.spores._
        |val interop = new JavaInteropEntrypoint()
        |val foo = interop.transientNonSerializable2
        |spore {
        |  val captured = foo
        |  () => captured
        |}
      """.stripMargin
    }
  }
}
