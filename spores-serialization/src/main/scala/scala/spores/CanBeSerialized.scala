package scala.spores

/** A witness that ensures to the spores spark macro that a value
  * is serializable without changing the signature of a type. */
trait CanBeSerialized[T]

trait SerializationWitnesses {
  implicit object ByteWitness extends CanBeSerialized[Byte]
  implicit object CharWitness extends CanBeSerialized[Char]
  implicit object ShortWitness extends CanBeSerialized[Short]
  implicit object IntWitness extends CanBeSerialized[Int]
  implicit object LongWitness extends CanBeSerialized[Long]
  implicit object DoubleWitness extends CanBeSerialized[Double]
  implicit object FloatWitness extends CanBeSerialized[Float]
}

object PrimitiveSerializationWitnesses extends SerializationWitnesses
