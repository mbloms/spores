import witness._
import spores._

class Data

object WitnessTest {
  import Spore._
  import Box._
  @main def test = {
    val lst: List[Int] = List(12,42,46,64)
    val arr: Array[Int] = Array(6,6,6)
    mkBox[Data] { packed =>
      implicit val acc = packed.access
      val actor = new ActorRef[Data] {
        override def send(msg: Box[Data])(cont: Spore[Unit,Unit] { type Excluded = msg.C; type CapturingWitness[T] = Immutable[T]})
        (implicit acc: CanAccess { type C = msg.C }): Nothing = throw new NoReturnControl
      }
      actor.send(packed.box)(spore {
        x =>
          println(lst)
      })
    }
  }
}