package scala.spores.spark

/** A witness that ensures to the spores spark macro that a value
  * is serializable without changing the signature of a type. */
sealed trait SerializationWitness[T]

/** Value types cannot extend `Serializable`, so an implicit witness
  * needs to exist in order to check if primitive types are serializable. */
trait SerializationWitnesses {
  implicit object ByteWitness extends SerializationWitness[Byte]
  implicit object CharWitness extends SerializationWitness[Char]
  implicit object ShortWitness extends SerializationWitness[Short]
  implicit object IntWitness extends SerializationWitness[Int]
  implicit object LongWitness extends SerializationWitness[Long]
  implicit object DoubleWitness extends SerializationWitness[Double]
  implicit object FloatWitness extends SerializationWitness[Float]
}

/** One can automatically import all the witnesses in the call site. */
object SerializationWitnesses extends SerializationWitnesses
