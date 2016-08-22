/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.whitebox

package object spores {

  object logger {
    def elem[T](es: sourcecode.Text[T]*)(implicit line: sourcecode.Line,
                                         file: sourcecode.File): Unit = {
      es.foreach { e =>
        val filename = file.value.replaceAll(".*/", "")
        println(s"$filename:${line.value} [${e.source}] ${e.value}")
      }
    }
  }

  // Change the default value to enable the macro debugging
  val defaultDebugProperty = System.getProperty("spores.debug", "true")
  private val isDebugEnabled = defaultDebugProperty.toBoolean
  private[spores] def debug(s: => String)(implicit line: sourcecode.Line,
                                          file: sourcecode.File): Unit = {
    if (isDebugEnabled) logger.elem(s)
  }

  /** Capture a variable and return it. */
  def capture[T](x: T): T = x

  /** Converts a block of statements and an anonymous function to a spore,
    * checking the captured paths and ensuring the spore semantics.
    *
    * The following spore will be returned if the body of the anonymous
    * function only accesses local variables or stable paths.
    *
    * {{{
    * spore {
    *   val x = outerX
    *   val y = outerY
    *   (p: T) => <body>
    * }
    * }}}
    */
  def spore[T, R](fun: T => R): Spore[T, R] = macro sporeImpl[T, R]

  def spore[T1, T2, R](fun: (T1, T2) => R): Spore2[T1, T2, R] =
    macro spore2Impl[T1, T2, R]

  def spore[T1, T2, T3, R](fun: (T1, T2, T3) => R): Spore3[T1, T2, T3, R] =
    macro spore3Impl[T1, T2, T3, R]

  def spore[R](fun: () => R): NullarySpore[R] = macro nullarySporeImpl[R]

  implicit def toSpore[T, R](fun: T => R): Spore[T, R] = macro sporeImpl[T, R]

  def delayed[T](body: T): () => T = () => body

  def nullarySporeImpl[R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[() => R]): ctx.Expr[NullarySpore[R]] = {
    import ctx.universe._
    val impl = new MacroImpl[ctx.type](ctx)
    val targs = List(weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[NullarySpore[R]](tree)
  }

  def sporeImpl[T: ctx.WeakTypeTag, R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[T => R]): ctx.Expr[Spore[T, R]] = {
    import ctx.universe._
    val impl = new MacroImpl[ctx.type](ctx)
    val targs = List(weakTypeOf[T], weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore[T, R]](tree)
  }

  def spore2Impl[T1: ctx.WeakTypeTag, T2: ctx.WeakTypeTag, R: ctx.WeakTypeTag](
      ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2) => R]): ctx.Expr[Spore2[T1, T2, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroImpl[ctx.type](ctx)
    val targs = List(weakTypeOf[T1], weakTypeOf[T2], weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore2[T1, T2, R]](tree)
  }

  def spore3Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3) => R]): ctx.Expr[Spore3[T1, T2, T3, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroImpl[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1], weakTypeOf[T2], weakTypeOf[T3], weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore3[T1, T2, T3, R]](tree)
  }
}
