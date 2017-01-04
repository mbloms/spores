package scala.spores.run.newtests

import scala.spores._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.reflect.{ClassTag, classTag}


sealed class CanAccess { type C }

object Box {
  def mkBox[T: ClassTag](fun: Packed[T] => Unit): Unit = {
    val cl = classTag[T].runtimeClass
    val instance: T = cl.newInstance().asInstanceOf[T]
    val theBox = new Box[T](instance)
    val packed = theBox.pack()
    fun(packed)
  }
}

sealed class Box[+T] private (private val instance: T) {
  self =>

  type C

  // trusted operation
  private def pack(): Packed[T] = new Packed[T] {
    val box: Box[T] = self
    implicit val access: CanAccess { type C = box.C } =
      new CanAccess { type C = box.C }
  }

  def open(fun: Spore[T, Unit])(implicit access: CanAccess { type C = self.C }): Box[T] = {
    fun(instance)
    self
  }

  def capture[S](consumed: Box[S])(
    fun: Spore[Packed[T], Unit] { type Excluded = consumed.C })(
    implicit access: CanAccess { type C = self.C },
      accessConsumed: CanAccess { type C = consumed.C }): Unit = {
    fun(pack())
  }
}

sealed trait Packed[+T] {
  val box: Box[T]
  implicit val access: CanAccess { type C = box.C }
}


class Data {
  var name: String = _
}

class Data2 {
  var num: Int = _
  var dat: Data = _
}

@RunWith(classOf[JUnit4])
class CaptureSpec {
  import Box._

  @Test
  def test(): Unit = {
    mkBox[Data] { packed =>
      implicit val acc = packed.access
      val box: packed.box.type = packed.box

      box.open { _.name = "John" }

      mkBox[Data2] { packed2 =>
        implicit val acc2 = packed2.access
        val box2: packed2.box.type = packed2.box

        box2.capture(box)(spore { (packedData: Packed[Data2]) =>
          implicit val accessData = packedData.access

          packedData.box.open { d =>
            assert(d.dat.name == "John")
          }
        })
      }
    }
  }

}
