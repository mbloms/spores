/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.spores

sealed trait SporeBase {

  /** Represent the type of the captured variables.
    *
    * For more than one captured variable, the type is a tuple of types.
    */
  type Captured

  /** Enable the creation of spores via reflection. */
  def className: String = _className
  protected[this] var _className: String = null
}

trait NullarySpore[+R] extends Function0[R] with SporeBase

trait NullarySporeWithEnv[+R] extends NullarySpore[R] {

  /** Store the environment of the Spore. */
  val captured: Captured
}

trait Spore[-T, +R] extends Function1[T, R] with SporeBase

trait SporeWithEnv[-T, +R] extends Spore[T, R] {

  /** Store the environment of the Spore. */
  val captured: Captured
}

trait Spore2[-T1, -T2, +R] extends Function2[T1, T2, R] with SporeBase

trait Spore2WithEnv[-T1, -T2, +R] extends Spore2[T1, T2, R] {

  /** Store the environment of the Spore. */
  val captured: Captured
}

trait Spore3[-T1, -T2, -T3, +R] extends Function3[T1, T2, T3, R] with SporeBase

trait Spore3WithEnv[-T1, -T2, -T3, +R] extends Spore3[T1, T2, T3, R] {

  /** Store the environment of the Spore. */
  val captured: Captured
}

