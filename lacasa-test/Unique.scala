package witness
package unique

import java.util.NoSuchElementException

import witness._

import scala.annotation.{Annotation, alpha, showAsInfix, infix}
import scala.collection.AbstractIterator
import spore.annotation.captureNothing
import witness.annotation._

trait Borrowed[+Owner,T] extends Witness[T] {
  def giveBack: Owner
}

/**
 * ShallowlyUnique witnesses that there are no external aliases of
 * the enclosed instance of type T.
 * Offers no guarantees for the members of the enclosed instance.
 * @tparam T
 */
trait ShallowlyUnique[+T] extends Witness[T] with ExtractOnce[T] {
  @throws[AlreadyConsumedException] @consumer
  def extract: T
}

/**
 * Unique[T] witnesses that there are no external aliases to the object of type T.
 * A T which is normally immutable could be safely mutated if contained in a Unique[T]
 * Every method call should return a new Unique[T], and use of the old reference should be invalid 
 * @tparam T
 */
abstract class Unique[+T] private (instance: T) extends FreshOnce[T] with Stealable[Unique[T]] {
  unique =>
  def this(o: Unique[T]) = this(o.extract)
  def borrow[B](spore: T => B @captureNothing) = new Borrowed[Unique[T],B] {
    private var owner: Unique[T] | Null = unique.steal
    private var borrowed: B | Null = spore(instance)
    def consume = {
      owner = null
      borrowed = null
    }
    def giveBack: Unique[T] = owner match {
      case null => throw new AlreadyConsumedException
      case _ => owner
    }
  }
  /*
  type Packed[_]
  def asBox(spore: Packed[T] => Unit): Nothing
   */
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
}

case object Empty extends List[Nothing] {
  override def head: Nothing = throw new NoSuchElementException("head of empty list")
  override def tail: Nothing = throw new UnsupportedOperationException("tail of empty list")
}

final class NonEmpty[+T] (val head: T, private var _tail: List[T]) extends List[T] {
  def tail: List[T] = _tail
  override def clone(): NonEmpty[T] = new NonEmpty[T](head,_tail)
}

object NonEmpty {
  implicit def Cloner[T]: Exorcist[NonEmpty[T],Mutable[T]] =
    new Exorcist[NonEmpty[T],Mutable[T]] {
      override def evictDemons(xs: NonEmpty[T]): Mutable[T] = new Mutable(xs)
    }

  /**
   * Provides a mutable interface for a contained NonEmpty node.
   * @param h list head
   * @param t list tail
   * @tparam T element type
   */
  class Mutable[T](h: T, t: List[T]) extends ShallowlyUnique[NonEmpty[T]] with Extract[NonEmpty[T]] {
    self =>

    /**
     * Provides a mutable interface for a contained NonEmpty node.
     * @param prev
     */
    private class Node extends ShallowlyUnique[NonEmpty[T]] {
      @secret
      private var prev: Node = _
      @secret
      private var _node: NonEmpty[T] = _
      private def node_=(xs: NonEmpty[T]): Unit = {
        _node = new NonEmpty[T](xs.head,xs.tail)
        if (prev != null)
          prev.tail = _node
      }

      def this(xs: NonEmpty[T]) = {
        this()
        node = xs
      }

      def this(prev: Node) = {
        this()
        this.prev = prev
        node = prev.tail match {
          case xs: NonEmpty[T] => xs
          case _ => null
        }
      }

      /**
       * Extract a shallow copy of the contained list
       * @return an instance of T
       */
      def extract = new NonEmpty[T](head,tail)
      override def clone: Node = new Node(_node)
      def head: T = node.head
      def head_=(x: T) = {
        node = new NonEmpty[T](x,tail)
      }
      def tail: List[T] = node.tail
      def tail_=(xs: List[T]) = node._tail = xs
    }
    def this(list: List[T]) = this(list.head,list.tail)
    @secret
    private var node: NonEmpty[T] = new NonEmpty[T](h,t)

    override def extract = ???
  }
    /*
    def borrow_tail = new Borrowed[T] {
      // ShallowlyUnique after clone
      @consumable
      private val root: Mutable[T] = self.clone
      private var prev: Mutable[T] = root
      private var _current: Mutable[T] = _
      private def current = _current
      private def current_=(xs: NonEmpty[T]): Unit = {
        _current = new Mutable[T](xs)
        prev.tail = _current.node
      }
      current = prev.tail
      
      def head_=(x: T): Unit = current.head=x
      def tail_=(xs: List[T]) = current.tail=xs
    }
  }

  trait Borrowed[T] extends witness.unique.Borrowed[Mutable[T],NonEmpty[T]]*/


    /*
    class MutableEnd[T](t: Mutable[T]) extends witness.unique.Borrowed[Mutable[T],NonEmpty[T]] with Stealable[MutableEnd[T]] {
      // ShallowlyUnique after clone
      private var root: Mutable[T] = xs.clone
      @secret
      private var finger: List[T] = root.clone
      //
      root.tail = finger
      
      def tail_=(xs: List[T]): Unit
    }*/
  /*
  @alpha("append")
  def [T] (xs: Unfinished[T]) :+  (x: T) : Unfinished[T] = {
    val next = new NonEmpty(x, null)
    val root = xs.root
    val last = xs.last
    last.synchronized {
      xs.consume
      //last.next = next
    }
    new Unfinished(root,next)
  }
  class Unfinished[T] private[NonEmpty]
    (@consumable private[NonEmpty] var root: List[T], @consumable private[NonEmpty] var last: NonEmpty[T])
    extends ShallowlyUnique[List[T]] {
    self =>
    
    private def this(xs: NonEmpty[T]) = this(xs,xs)
    def this(h: T) = this(new NonEmpty(h,null))
    
    override def steal: Unfinished[T] = consumeAs(new Unfinished(root,last))
    override def extract: List[T] = consumeAs(root)
    
    def setNext(xs: List[T]) = last.next = xs

    /** call consume() and return the argument */
    def consumeAs[R](ret: R): R = {consume; ret}
    override def consume: Unit =
      synchronized {
        if (root == null)
          then throw new AlreadyConsumedException
          else {root = null; last = null}
      }
  }*/
}