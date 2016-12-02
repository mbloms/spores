package scala.spores.serialization.run.capture

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

/* Tests implicit search of `CanSerialize` in situations where
 * the implicits are defined in different parts of the program. */

trait Base {
  sealed class NoJavaSerializable(val s: String)
  implicit object NoJavaSerializableWillBeSerializableSomehow
      extends CanSerialize[NoJavaSerializable]

  sealed class NoJavaSerializable2(val s: String)
  implicit object NoJavaSerializable2WillBeSerializableSomehow
    extends CanSerialize[NoJavaSerializable2]
}

@RunWith(classOf[JUnit4])
class CanSerializeSpec extends Base {
  @Test
  def `Detect implicit canserialize for non-serializable type I`(): Unit = {
    val instance = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      () =>
        nj
    }
    assert(s() == instance)
  }

  @Test
  def `Detect implicit canserialize for several non-serializable types I`(): Unit = {
    val instance = new NoJavaSerializable2("tada")
    val instance2 = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      val nj2 = instance2
      () => nj.s + nj2.s
    }
    assert(s() == "tadatada")
  }
}

@RunWith(classOf[JUnit4])
class CanSerializeSpec2 {
  sealed class NoJavaSerializable(val s: String)
  implicit object NoJavaSerializableWillBeSerializableSomehow
    extends CanSerialize[NoJavaSerializable]

  sealed class NoJavaSerializable2(val s: String)
  implicit object NoJavaSerializable2WillBeSerializableSomehow
    extends CanSerialize[NoJavaSerializable2]

  @Test
  def `Detect implicit canserialize for non-serializable type II`(): Unit = {
    val instance = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      () =>
        nj
    }
    assert(s() == instance)
  }

  @Test
  def `Detect implicit canserialize for several non-serializable types II`(): Unit = {
    val instance = new NoJavaSerializable2("tada")
    val instance2 = new NoJavaSerializable("tada")
    val s = spore {
      val nj = instance
      val nj2 = instance2
      () => nj.s + nj2.s
    }
    assert(s() == "tadatada")
  }
}
