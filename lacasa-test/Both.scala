package witness

class Both[+Left <: Witness[T],+Right <: Witness[T],+T](val left: Left, val right: Right) extends Witness[T]

object Both {
  implicit def prover[Right <: Witness[T],Left <: Witness[T],T]
    (implicit le: Exorcist[T,Left], re: Exorcist[T,Right])
    : Exorcist[T,Both[Left,Right,T]] =
      new Exorcist[T,Both[Left,Right,T]] {
        override def evictDemons(x: T) =
          new Both[Left, Right, T](le.evictDemons(x),re.evictDemons(x))
      }
}
