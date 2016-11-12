package scala.spores

/** A witness that ensures to the spores spark macro that a value
  * is serializable without changing the signature of a type. */
trait AssumedSerializable[T]
