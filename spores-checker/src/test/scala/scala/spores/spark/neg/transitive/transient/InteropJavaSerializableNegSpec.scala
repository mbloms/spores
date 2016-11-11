package scala.spores.spark.neg.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores.spark.TestUtil._
import scala.spores.util.Feedback._

@RunWith(classOf[JUnit4])
class InteropJavaSerializableNegSpec {
  @Test
  def `Non serializable fields in classes are detected`(): Unit = {
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
}
