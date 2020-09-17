package witness
package unique
package nursery

import java.util.NoSuchElementException
import java.lang.System.identityHashCode

import scala.annotation.{alpha,infix}
import witness.annotation.{internal, secret}
import witness.unique.nursery.ShallowlyUnique.UnmatchingInstanceError

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.AbstractIterator

class ShallowlyUnique[+T <: AnyRef] private[witness] (_instance: T) {
  private var instance: _instance.type = _instance
  def refersTo[B >: T <: AnyRef](that: B): Boolean = instance eq that
  def equals[B >: T <: AnyRef](that: ShallowlyUnique[B]): Boolean = this.instance eq that.instance
  override def hashCode: Int = identityHashCode(instance)
}

object ShallowlyUnique {
  def unsafe[T <: AnyRef](x: T): ShallowlyUnique[x.type] = ShallowlyUnique[x.type](x)
  class UnmatchingInstanceError(str: String = "ShallowlyUnique[x.type] did not refer to x") extends Error
}

/**
 * Guarantees that the dereferenced instance is
 * shallowly unique if used correctly
 * There must never be more than one referencing the same instance 
 */
trait ShallowUniqueRef[T] {
  /** reference is unique is as long as result is not saved */
  def deref: T
  
  /** replace contained referece with that */
  @alpha("assign")
  def :=(that: T): Unit
}

/**
 * Provides a safe mutable interface to the enclosed reference
 * Must not provide a way to leak the contained reference!!
 * @tparam T
 */
abstract class MutRef[T] {
  self =>
  /** reference to enclosed reference */
  private[witness] var _ref: T = _
  /** ref must only be updated  */
  private[witness] def ref_=(newRef: T): Unit = _ref = newRef
  private[unique] def ref: T = _ref
  /**
   * Assign all mutable fields in this to that 
   * @param that
   * @return true if this and that compare equal
   */
  def mimic(that: T): Boolean
}

sealed abstract class List[+T] {
  self =>
  def head: T
  def tail: List[T]
  def iterator: Iterator[T] = new AbstractIterator[T] {
    private var xs: List[T] = self
    override def hasNext: Boolean = xs != null && xs.isInstanceOf[NonEmpty[T]]
    override def next(): T = {
      val x = xs.head
      xs = xs.tail
      x
    }
  }
}

object :: {
  def unapply[T](list: List[T]): Option[(T,List[T])] = try {
    Some(list.head,list.tail)
  } catch {
    case _: NullPointerException => None
    case _: NoSuchElementException => None
    case _: UnsupportedOperationException => None
  }
  def apply[T](x: T, xs: List[T]): NonEmpty[T] = new NonEmpty[T](x,xs)
}

case object Empty extends List[Nothing] {
  override def head: Nothing = throw new NoSuchElementException("head of empty list")
  override def tail: Nothing = throw new UnsupportedOperationException("tail of empty list")
}

final class NonEmpty[+T] (val head: T, private var _tail: List[T]) extends List[T] {
  def tail: List[T] = _tail
  def tail_=(xs: List[T] @uncheckedVariance)(implicit ev: ShallowlyUnique[this.type]): Unit =
    if (!ev.refersTo(this))
      throw new UnmatchingInstanceError
    else
      _tail = xs
  override def clone(): NonEmpty[T] = new NonEmpty[T](head,_tail)
}

object NonEmpty {

  /**
   * Provides a mutable interface for a contained NonEmpty node.
   * @param h list head
   * @param t list tail
   * @tparam T element type
   */
  class MutableNonEmpty[T] extends MutRef[NonEmpty[T]] {
    val index = 0
    @internal
    private var root: List[T] = Empty

    override def mimic(that: NonEmpty[T]): Boolean = ???
  
    def head = root.head
    def head_=(x: T) = root = new NonEmpty(x,root match {
      case _ :: xs => xs
      case _ => Empty
    })
    def tail = root.tail
    // Ok because root is internal
    def tail_=(xs: List[T]) = root.asInstanceOf[NonEmpty[T]]._tail=xs
  }
}

class Stack[T] {
  private val stack: Array[T] = ???
  private var divider: Int = ???
  // Elements before the divider are shallowly unique
}

