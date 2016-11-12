package scala.spores.spark.run.transitive.transient

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class InteropJavaSerializableSpec {
  @Test
  def `Non serializable fields in classes are detected`(): Unit = {
    val interop = new JavaInteropEntrypoint()
    val foo = interop.transientSerializable
    spore {
      val captured = foo
      () =>
        captured
    }
  }
}
