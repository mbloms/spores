package scala.spores.serialization.run.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class InteropJavaSerializableSpec {
  @Test
  def `Java defined transient fields in classes are ignored`(): Unit = {
    val interop = new JavaInteropEntrypoint()
    val foo = interop.transientSerializable
    val s = spore {
      val captured = foo
      () =>
        captured
    }
  }
}
