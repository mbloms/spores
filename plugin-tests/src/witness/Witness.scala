package witness

import scala.language.implicitConversions
import scala.reflect._
import scala.collection._
import scala.collection.immutable._
import mutable.Clearable

/**
 * Base trait for a Captured instance of type T
 * TODO: Naming
 * @tparam T
 */
trait Witness[+T] extends Any

/* Maybe differentiate between user made Witnesses and Predefined
sealed trait ByDefinition[+T] extends Witness[T]
sealed trait InferredWitness[+T] extends Witness[T]
open trait UserWitness[+T] extends Witness[T]
*/

//TODO: trait Exorcist[+F[_] <: Witness[_],T] breaks implicit resolution
/**
 * Type class for lifting types into a Captured
 * @tparam F
 * @tparam T
 */
trait Exorcist[T,+W <: Witness[T]] {
  def evictDemons(x: T): W
}

//type Lifter[T] = [F[T] <: Witness[T]] =>> Exorcist[T,F[T]]

class AlreadyConsumedException(error: String) extends RuntimeException(error) {
  def this() = this("Contained value has already been consumed and is no longer valid")
}

/** Offer an extract method that can be called at least once */
trait ExtractOnce[+T] extends Any {
  /**
   * Extract an instance of T, may fail if called more than once.
   * @throws Throwable might be thrown if called more than once
   * @return an instance of T
   */
  @throws[Throwable]
  def extract: T
}

/** Wrapper around T, offers no guarantees */
trait Extract[+T] extends Any with ExtractOnce[T] {
  /**
   * Extract an instance of T, can be used multiple times.
   * Should not throw Exceptions
   * @return an instance of T
   */
  @throws[Nothing]
  override def extract: T
}

trait Consumable extends Any {
  /** Consume object, making all future use invalid */
  def consume: Unit
}

trait Stealable[+Stolen] extends Any with Consumable {
  /**
   * Invalidate the witness by making the enclosed value unreachable
   */
  override def consume: Unit

  /**
   * Invalidate the witness and return a new one with the enclosed value unchanged
   * @throws AlreadyConsumedException if the witness was already consumed.
   * @return A fresh Stealable reference
   */
  @throws[AlreadyConsumedException]
  def steal: Stolen
}

trait FreshOnce[+T] extends Any with Witness[T] with ExtractOnce[T] {
  /**
   * Extract the fresh value.
   * Can only be called once!
   * @throws AlreadyConsumedException if extract has already been called
   * @return an instance of T that is guaranteed to have no mutating aliases
   */
  @throws[AlreadyConsumedException]
  override def extract: T
}

final class Cached[+W <: Extract[T],+T](val recover: W) extends Witness[T] with Extract[T] {
  lazy val extract: T = recover.extract
}

object Cached {
  implicit def lifter[T,W <: Witness[T] & Extract[T]](implicit inner: Exorcist[T,W]): Exorcist[T,Cached[W,T]] =
    new Exorcist {
      override def evictDemons(x: T): Cached[W, T] = new Cached(inner.evictDemons(x))
    }
}

/** Alternative to FreshOnce, not technically a witness */
trait ConsumableFresh[+T] extends Any with FreshOnce[Option[T]] with Consumable {
  /**
   * Extract a fresh value, usually a copy of the contained value
   * Does not consume the contained value.
   * @throws NoSuchElementException if the contained value has been consumed
   * @return an instance of T that is guaranteed to have no mutating aliases
   */
  @throws[NoSuchElementException]
  override def extract: Option[T]
  @throws[NoSuchElementException]
  def steal: T = extract.get
}

trait ToFresh[+T] {
  def fresh: Fresh[T]
}

object FreshOnce {
  import scala.collection.mutable._
  case object ElementConsumedException extends NoSuchElementException("value has already been consumed")

  implicit def shallowCloner[A,CC[A] <: Cloneable[CC[A]]](using Exorcist[A,Immutable[A]]): Exorcist[CC[A],FreshOnce[CC[A]]] =
    new Exorcist {
      override def evictDemons(x: CC[A]): FreshOnce[CC[A]] =
        new FreshOnce[CC[A]] with ToFresh[CC[A]] {
          self =>
          private var copy: Option[CC[A]] = Some(x.clone())
          override def extract: CC[A] = copy match {
            case Some(xs) =>
              copy = None
              xs
            case _ => throw ElementConsumedException
          }
          override def fresh: Fresh[CC[A]] =
            new Fresh[CC[A]] {
              private val copy = self.extract
              override def extract: CC[A] = copy.clone()
            }
        }
    }
  implicit def deepCloner[A,CC[A] <: IterableOnceOps[A,CC,CC[A]]](using ev: Exorcist[A,FreshOnce[A]]): Exorcist[CC[A],FreshOnce[CC[A]]] =
    new Exorcist {
      override def evictDemons(x: CC[A]): FreshOnce[CC[A]] =
        new FreshOnce[CC[A]] {
          private var copy: Option[CC[A]] = Some(x.map(ev.evictDemons(_).extract))
          override def extract: CC[A] = copy match {
            case Some(xs) =>
              copy = None
              xs
            case _ => throw ElementConsumedException
          }
        }
    }
}

//TODO: Should Fresh be named pure and FreshOnce be just Fresh?
/**
 * Fresh[T] witnesses that the instance of T in this wrapper is "fresh".
 *
 * The contain instance must either be guaranteed to be immutable (Frozen)
 * or a new instance that is created every time "extract" is called.
 *
 * @tparam T
 */
sealed trait Fresh[+T] extends FreshOnce[T] {
  override def extract: T
}

object Fresh {
  def fresh[T: ClassTag]: Fresh[T] =
    new Fresh[T] {
      override def extract: T =
        classTag[T].runtimeClass.newInstance().asInstanceOf[T]
    }
  // The closure must only capture Fresh values
  def asFresh[T](closure: => T): Fresh[T] =
    new Fresh[T] {
      override def extract: T = closure
    }

  import scala.collection.mutable._
  implicit def shallowCloner[A,CC[A] <: Cloneable[CC[A]]](using Exorcist[A,Immutable[A]]): Exorcist[CC[A],Fresh[CC[A]]] =
    new Exorcist {
      override def evictDemons(x: CC[A]): Fresh[CC[A]] =
        new Fresh[CC[A]] {
          private val copy: CC[A] = x.clone()
          override def extract: CC[A] = copy.clone()
        }
    }
  implicit def deepCloner[A,CC[A] <: IterableOnceOps[A,CC,CC[A]]](using ev: Exorcist[A,Fresh[A]]): Exorcist[CC[A],Fresh[CC[A]]] =
    new Exorcist {
      override def evictDemons(x: CC[A]): Fresh[CC[A]] =
        new Fresh[CC[A]] {
          private val copy: CC[Fresh[A]] = x.map(ev.evictDemons(_))
          override def extract: CC[A] = copy.map(_.extract)
        }
    }
}

// Using `T <: Serializable` breaks
// upper bound [_$1] =>> witness.Witness[?]

sealed class Serialized[+T] private (original: T) extends Fresh[T] with Serializable {
  import java.io._
  private val serialized: Array[Byte] = {
    val stream = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(stream)
    out.writeObject(original)
    out.close()
    stream.toByteArray
  }
  def objectInputStream: ObjectInputStream = {
    new ObjectInputStream(new ByteArrayInputStream(serialized))
  }
  override def extract: T = {
    objectInputStream.readObject.asInstanceOf[T]
  }
}

object Serialized {
  def apply[T <: Serializable](x: T): Serialized[T] = new Serialized[T](x)
  def unapply[T](serialized: Serialized[T]): Some[T] = Some(serialized.extract)
  implicit def serializer[A <: Serializable]: Exorcist[A,Serialized[A]] =
    new Exorcist {
      override def evictDemons(x: A): Serialized[A] =
        new Serialized[A](x)
  }
}

/**
 * Frozen[T] witnesses that the instance of T in this wrapper is immutable.
 * This does not neccecarily mean that all T are immutable.
 *
 * The contained instance must be "frozen" in the sense that
 * it must not be possible to make it mutable without making a new instance.
 *
 * @tparam T
 */
sealed trait Frozen[+T] extends Fresh[T]

/**
 * An instance Immutable[T] witnesses that _all_ instances of exactly T is Immutable.
 * Unlike Frozen[T], Immutable[T] is invariant in T:
 * Immutable[Nothing] is a subtype of Frozen[Any], but not of Immutable[Any],
 * because that would mean all instances of Any is immutable.
 * @tparam T
 */
sealed trait Immutable[T] extends Frozen[T]

// This is the sickest thing ever
/** Lift[F] is a function T => F[T]*/
type Lift[F[_]] = [T] =>> T => F[T]

/**
 * For all instances of a class to be (deeply) immutable,
 * it must not have any (externally) mutable state.
 * Since a subclass can add mutable fields and methods,
 * for all instances of a class to be immutable, it must either be final,
 * or it sealed with only immutable subclasses.
 */
object Immutable {
  // I mean where did all the boilerplate go??
  type Primitive = scala.Double
                 | scala.Float
                 | scala.Long
                 | scala.Int
                 | scala.Char
                 | scala.Short
                 | scala.Byte
                 | scala.Boolean
                 | scala.Unit
  type SafeBottom = scala.Null
                  | scala.Nothing
                  | scala.collection.immutable.Nil.type
                  | scala.None.type
  type SafeContainer = [X] =>> scala.collection.immutable.List[X]
                             | scala.Option[X]

  def apply[T](x: T)(using exorcist: Exorcist[T,Immutable[T]]): Immutable[T] = {
    exorcist.evictDemons(x)
  }
  def unapply[T](pure: Immutable[T]): Some[T] = Some(pure.extract)
  def unapply[T](x: T)(using ev: Exorcist[T,Immutable[T]]): Some[T] = Some(x)

  implicit def ImmutabilityBlesser[T](implicit bless: T => Immutable[T]): Exorcist[T,Immutable[T]] =
    new Exorcist {
      override def evictDemons(x: T): Immutable[T] = bless(x)
    }

  // Actually, there are classes like ArrayOps that extend AnyVal, but are mutable :(
  //implicit class ImmutableVal[T <: AnyVal](override val extract: T) extends Immutable[T]

  //TODO: Think hard about value classes
  implicit class ImmutablePrimitive[T <: Primitive](override val extract: T) extends Immutable[T]
  implicit class ImmutableBottom[T <: SafeBottom](override val extract: T) extends Immutable[T]
  implicit class ImmutableContainer[T,F[T] <: SafeContainer[T]](override val extract: F[T])(using Exorcist[T,Immutable[T]]) extends Immutable[F[T]]
}

/**
 * A Freezer takes an instance of type T, which might not be immutable,
 * and returns an instance of T wrapped in Immutable.
 * The returned instance might be a copy to guarantee immutability
 * @tparam T
 */
type Freezer[T] = Exorcist[T,Frozen[T]]

object Frozen {
  import scala.collection.ArrayOps
  import scala.reflect.ClassTag

  def apply[T](x: T)(implicit freezer: Freezer[T]): Frozen[T] = freezer.evictDemons(x)

  implicit def BlessedFreezer[T](using blesser: Exorcist[T,Immutable[T]]): Exorcist[T,Frozen[T]] = blesser
  
  implicit def subconv[T](x: T)(implicit ev: T => Immutable[T]): Frozen[T] = ev(x)

  private def asFrozen[T](frozen: T): Frozen[T] = new Frozen[T] {
    override def extract: T = frozen
  }
  //private class FrozenInstance[+T](override val extract: T) extends Immutable[T]

  //implicit class ImplicitFreezer[T](implicit convert: T => Frozen[T]) extends Freezer[T] {
  //  override def evictDemons(x: T): Frozen[T] = convert(x)
  //}
  implicit def ArrayOpsFreezer[A](implicit freezer: Freezer[A], classTag: ClassTag[A]): Freezer[ArrayOps[A]] =
    new Freezer[ArrayOps[A]] {
      override def evictDemons(xs: ArrayOps[A]): Frozen[ArrayOps[A]] = {
        val copy = xs.toArray
        asFrozen(new ArrayOps[A](copy))
      }
    }
}