/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala

import scala.language.experimental.macros
import scala.language.{implicitConversions, postfixOps}
import scala.reflect.macros.whitebox
import scala.spores.spark.SerializationWitnesses
import scala.util.Try

package object spores extends SerializationWitnesses {

  val version = "0.3.0-b1"

  object logger {
    def elem[T](es: sourcecode.Text[T]*)(implicit line: sourcecode.Line,
                                         file: sourcecode.File): Unit = {
      es.foreach { e =>
        val filename = file.value.replaceAll(".*/", "")
        val header = Console.GREEN + s"$filename:${line.value}"
        val source = Console.MAGENTA + s"[${e.source}]"
        println(s"$header $source ${Console.RESET}${e.value}")
      }
    }
  }

  def settingToBoolean(expectedBoolean: String, default: => Boolean = false) = {
    Try { expectedBoolean.toBoolean } recover {
      // If value cannot be parsed as Boolean, default to false
      case t: Throwable => default
    } get
  }

  private val enabledSpark = System.getProperty("spores.spark")
  val isSparkEnabled = settingToBoolean(enabledSpark, default = false)

  // Change the default value to enable the macro debugging
  private val defaultDebugProperty = System.getProperty("spores.debug")
  val isDebugEnabled = settingToBoolean(defaultDebugProperty, default = true)
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

  def spore[T1, T2, T3, T4, R](
      fun: (T1, T2, T3, T4) => R): Spore4[T1, T2, T3, T4, R] =
    macro spore4Impl[T1, T2, T3, T4, R]

  def spore[T1, T2, T3, T4, T5, R](
      fun: (T1, T2, T3, T4, T5) => R): Spore5[T1, T2, T3, T4, T5, R] =
    macro spore5Impl[T1, T2, T3, T4, T5, R]

  def spore[T1, T2, T3, T4, T5, T6, R](
      fun: (T1, T2, T3, T4, T5, T6) => R): Spore6[T1, T2, T3, T4, T5, T6, R] =
    macro spore6Impl[T1, T2, T3, T4, T5, T6, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, R](
      fun: (T1, T2, T3, T4, T5, T6,
            T7) => R): Spore7[T1, T2, T3, T4, T5, T6, T7, R] =
    macro spore7Impl[T1, T2, T3, T4, T5, T6, T7, R]

  def spore[R](fun: () => R): NullarySpore[R] = macro nullarySporeImpl[R]

  implicit def toSpore[T, R](fun: T => R): Spore[T, R] = macro sporeImpl[T, R]

  def delayed[T](body: T): () => T = () => body

  def nullarySporeImpl[R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[() => R]): ctx.Expr[NullarySpore[R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs = List(weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[NullarySpore[R]](tree)
  }

  def sporeImpl[T: ctx.WeakTypeTag, R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[T => R]): ctx.Expr[Spore[T, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs = List(weakTypeOf[T], weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore[T, R]](tree)
  }

  def spore2Impl[T1: ctx.WeakTypeTag, T2: ctx.WeakTypeTag, R: ctx.WeakTypeTag](
      ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2) => R]): ctx.Expr[Spore2[T1, T2, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroModule[ctx.type](ctx)
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
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1], weakTypeOf[T2], weakTypeOf[T3], weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore3[T1, T2, T3, R]](tree)
  }

  def spore4Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4) => R])
    : ctx.Expr[Spore4[T1, T2, T3, T4, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore4[T1, T2, T3, T4, R]](tree)
  }

  def spore5Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 T5: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5) => R])
    : ctx.Expr[Spore5[T1, T2, T3, T4, T5, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore5[T1, T2, T3, T4, T5, R]](tree)
  }

  def spore6Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 T5: ctx.WeakTypeTag,
                 T6: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6) => R])
    : ctx.Expr[Spore6[T1, T2, T3, T4, T5, T6, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore6[T1, T2, T3, T4, T5, T6, R]](tree)
  }

  def spore7Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 T5: ctx.WeakTypeTag,
                 T6: ctx.WeakTypeTag,
                 T7: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7) => R])
    : ctx.Expr[Spore7[T1, T2, T3, T4, T5, T6, T7, R]] = {
    import ctx.universe._

    // check Spore constraints
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[T7],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore7[T1, T2, T3, T4, T5, T6, T7, R]](tree)
  }
}
