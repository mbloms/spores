package scala.spores.serialization.run.capture

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

trait Base {
  sealed class NoJavaSerializable(val s: String)
  implicit object NoJavaSerializableWillBeSerializableSomehow
      extends CanSerialize[NoJavaSerializable]
}

@RunWith(classOf[JUnit4])
class CanSerializeSpec extends Base {
  @Test
  def `Detect implicit canserialize for non-serializable type`(): Unit = {
    val instance = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      () =>
        nj
    }
    assert(s() == instance)
  }
}

@RunWith(classOf[JUnit4])
class CanSerializeSpec2 {
  sealed class NoJavaSerializable(val s: String)
  implicit object NoJavaSerializableWillBeSerializableSomehow
    extends CanSerialize[NoJavaSerializable]

  @Test
  def `Detect implicit canserialize for non-serializable type`(): Unit = {
    val instance = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      () =>
        nj
    }
    assert(s() == instance)
  }
}
