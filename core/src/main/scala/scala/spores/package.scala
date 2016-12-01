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
import scala.spores.util.Versioning
import scala.util.Try

package object spores extends Versioning {
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

  // TODO(jvican): Change this for compiler flag
  // Change the default value to enable the macro debugging
  private val defaultDebugProperty = System.getProperty("spores.debug")
  val isDebugEnabled = settingToBoolean(defaultDebugProperty)
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

  // FORMAT: OFF
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

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, R](
      fun: (T1, T2, T3, T4, T5, T6, T7,
            T8) => R): Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R] =
    macro spore8Impl[T1, T2, T3, T4, T5, T6, T7, T8, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8,
            T9) => R): Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] =
    macro spore9Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9,
            T10) => R): Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] =
    macro spore10Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] =
    macro spore11Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] =
  macro spore12Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] =
  macro spore13Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): Spore14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] =
  macro spore14Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): Spore15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] =
  macro spore15Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): Spore16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R] =
  macro spore16Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): Spore17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R] =
  macro spore17Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): Spore18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R] =
  macro spore18Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): Spore19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R] =
  macro spore19Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): Spore20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R] =
  macro spore20Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): Spore21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R] =
  macro spore21Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]

  def spore[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](
      fun: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): Spore22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R] =
  macro spore22Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]

  // FORMAT: ON

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

  def spore8Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 T5: ctx.WeakTypeTag,
                 T6: ctx.WeakTypeTag,
                 T7: ctx.WeakTypeTag,
                 T8: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => R])
    : ctx.Expr[Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[T7],
           weakTypeOf[T8],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R]](tree)
  }

  def spore9Impl[T1: ctx.WeakTypeTag,
                 T2: ctx.WeakTypeTag,
                 T3: ctx.WeakTypeTag,
                 T4: ctx.WeakTypeTag,
                 T5: ctx.WeakTypeTag,
                 T6: ctx.WeakTypeTag,
                 T7: ctx.WeakTypeTag,
                 T8: ctx.WeakTypeTag,
                 T9: ctx.WeakTypeTag,
                 R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => R])
    : ctx.Expr[Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[T7],
           weakTypeOf[T8],
           weakTypeOf[T9],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]](tree)
  }

  def spore10Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R])
    : ctx.Expr[Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[T7],
           weakTypeOf[T8],
           weakTypeOf[T9],
           weakTypeOf[T10],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]](tree)
  }

  def spore11Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
      fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R])
    : ctx.Expr[Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
           weakTypeOf[T2],
           weakTypeOf[T3],
           weakTypeOf[T4],
           weakTypeOf[T5],
           weakTypeOf[T6],
           weakTypeOf[T7],
           weakTypeOf[T8],
           weakTypeOf[T9],
           weakTypeOf[T10],
           weakTypeOf[T11],
           weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]](tree)
  }

  def spore12Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R])
  : ctx.Expr[Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]](tree)
  }

  def spore13Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R])
  : ctx.Expr[Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]](tree)
  }

  def spore14Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R])
  : ctx.Expr[Spore14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]](tree)
  }

  def spore15Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R])
  : ctx.Expr[Spore15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]](tree)
  }

  def spore16Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R])
  : ctx.Expr[Spore16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]](tree)
  }

  def spore17Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R])
  : ctx.Expr[Spore17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]](tree)
  }

  def spore18Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  T18: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R])
  : ctx.Expr[Spore18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[T18],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]](tree)
  }

  def spore19Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  T18: ctx.WeakTypeTag,
                  T19: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R])
  : ctx.Expr[Spore19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[T18],
        weakTypeOf[T19],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]](tree)
  }

  def spore20Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  T18: ctx.WeakTypeTag,
                  T19: ctx.WeakTypeTag,
                  T20: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R])
  : ctx.Expr[Spore20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[T18],
        weakTypeOf[T19],
        weakTypeOf[T20],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]](tree)
  }

  def spore21Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  T18: ctx.WeakTypeTag,
                  T19: ctx.WeakTypeTag,
                  T20: ctx.WeakTypeTag,
                  T21: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R])
  : ctx.Expr[Spore21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[T18],
        weakTypeOf[T19],
        weakTypeOf[T20],
        weakTypeOf[T21],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]](tree)
  }

  def spore22Impl[T1: ctx.WeakTypeTag,
                  T2: ctx.WeakTypeTag,
                  T3: ctx.WeakTypeTag,
                  T4: ctx.WeakTypeTag,
                  T5: ctx.WeakTypeTag,
                  T6: ctx.WeakTypeTag,
                  T7: ctx.WeakTypeTag,
                  T8: ctx.WeakTypeTag,
                  T9: ctx.WeakTypeTag,
                  T10: ctx.WeakTypeTag,
                  T11: ctx.WeakTypeTag,
                  T12: ctx.WeakTypeTag,
                  T13: ctx.WeakTypeTag,
                  T14: ctx.WeakTypeTag,
                  T15: ctx.WeakTypeTag,
                  T16: ctx.WeakTypeTag,
                  T17: ctx.WeakTypeTag,
                  T18: ctx.WeakTypeTag,
                  T19: ctx.WeakTypeTag,
                  T20: ctx.WeakTypeTag,
                  T21: ctx.WeakTypeTag,
                  T22: ctx.WeakTypeTag,
                  R: ctx.WeakTypeTag](ctx: whitebox.Context)(
    fun: ctx.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R])
  : ctx.Expr[Spore22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]] = {
    import ctx.universe._
    val impl = new MacroModule[ctx.type](ctx)
    val targs =
      List(weakTypeOf[T1],
        weakTypeOf[T2],
        weakTypeOf[T3],
        weakTypeOf[T4],
        weakTypeOf[T5],
        weakTypeOf[T6],
        weakTypeOf[T7],
        weakTypeOf[T8],
        weakTypeOf[T9],
        weakTypeOf[T10],
        weakTypeOf[T11],
        weakTypeOf[T12],
        weakTypeOf[T13],
        weakTypeOf[T14],
        weakTypeOf[T15],
        weakTypeOf[T16],
        weakTypeOf[T17],
        weakTypeOf[T18],
        weakTypeOf[T19],
        weakTypeOf[T20],
        weakTypeOf[T21],
        weakTypeOf[T22],
        weakTypeOf[R])
    val tree = impl.createSpore(fun.tree, targs)
    ctx.Expr[Spore22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]](tree)
  }
}
