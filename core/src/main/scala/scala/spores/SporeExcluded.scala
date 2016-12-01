package scala.spores

import scala.language.implicitConversions
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.spores.util.Feedback

/** Implicit conversion between spores and spores with excluded types. */
object Conversions {
  // TODO(jvican): Consider importing all this into the spores package
  implicit def toExcluded[T, R, A](s: Spore[T, R]): Spore[T, R] {
    type Excluded = A
  } = macro SporeTranslator.toExcludedSpore[T, R, A]

  implicit def toExcluded[R, A](s: NullarySpore[R]): NullarySpore[R] {
    type Excluded = A
  } = macro SporeTranslator.toExcludedNullary[R, A]

  implicit def toExcluded[T1, T2, R, A](
      s: Spore2[T1, T2, R]): Spore2[T1, T2, R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore2[T1, T2, R, A]

  implicit def toExcluded[T1, T2, T3, R, A](
      s: Spore3[T1, T2, T3, R]): Spore3[T1, T2, T3, R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore3[T1, T2, T3, R, A]

  implicit def toExcluded[T1, T2, T3, T4, R, A](
      s: Spore4[T1, T2, T3, T4, R]): Spore4[T1, T2, T3, T4, R] {
    type Excluded = A
  } =
    macro SporeTranslator.toExcludedSpore4[T1, T2, T3, T4, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, R, A](
      s: Spore5[T1, T2, T3, T4, T5, R]): Spore5[T1, T2, T3, T4, T5, R] {
    type Excluded = A
  } =
    macro SporeTranslator.toExcludedSpore5[T1, T2, T3, T4, T5, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, R, A](s: Spore6[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    R]): Spore6[T1, T2, T3, T4, T5, T6, R] {
    type Excluded = A
  } =
    macro SporeTranslator.toExcludedSpore6[T1, T2, T3, T4, T5, T6, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, R, A](
      s: Spore7[T1, T2, T3, T4, T5, T6, T7, R]): Spore7[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        R] {
    type Excluded = A
  } =
    macro SporeTranslator.toExcludedSpore7[T1, T2, T3, T4, T5, T6, T7, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, R, A](
      s: Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R]): Spore8[T1,
                                                            T2,
                                                            T3,
                                                            T4,
                                                            T5,
                                                            T6,
                                                            T7,
                                                            T8,
                                                            R] {
    type Excluded = A
  } =
    macro SporeTranslator
      .toExcludedSpore8[T1, T2, T3, T4, T5, T6, T7, T8, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, R, A](
      s: Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]): Spore9[T1,
                                                                T2,
                                                                T3,
                                                                T4,
                                                                T5,
                                                                T6,
                                                                T7,
                                                                T8,
                                                                T9,
                                                                R] {
    type Excluded = A
  } =
    macro SporeTranslator
      .toExcludedSpore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R, A](
      s: Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]): Spore10[T1,
                                                                       T2,
                                                                       T3,
                                                                       T4,
                                                                       T5,
                                                                       T6,
                                                                       T7,
                                                                       T8,
                                                                       T9,
                                                                       T10,
                                                                       R] {
    type Excluded = A
  } =
    macro SporeTranslator
      .toExcludedSpore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R, A](
      s: Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]): Spore11[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    R] { type Excluded = A } =
    macro SporeTranslator
      .toExcludedSpore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R, A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R,
  A](s: Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]): Spore12[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore12[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, R, A](s: Spore13[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    R]): Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] {
    type Excluded = A
  } =
    macro SporeTranslator.toExcludedSpore13[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, R, A](
      s: Spore14[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 R]): Spore14[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore14[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, R, A](
      s: Spore15[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 R]): Spore15[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore15[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, R, A](
      s: Spore16[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 R]): Spore16[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore16[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, R, A](
      s: Spore17[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 R]): Spore17[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore17[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, T18, R, A](
      s: Spore18[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 T18,
                 R]): Spore18[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore18[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            T18,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, T18, T19, R, A](
      s: Spore19[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 T18,
                 T19,
                 R]): Spore19[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore19[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            T18,
                                            T19,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, T18, T19, T20, R, A](
      s: Spore20[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 T18,
                 T19,
                 T20,
                 R]): Spore20[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore20[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            T18,
                                            T19,
                                            T20,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, T18, T19, T20, T21, R, A](
      s: Spore21[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 T18,
                 T19,
                 T20,
                 T21,
                 R]): Spore21[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              T21,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore21[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            T18,
                                            T19,
                                            T20,
                                            T21,
                                            R,
                                            A]

  implicit def toExcluded[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,
  T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R, A](
      s: Spore22[T1,
                 T2,
                 T3,
                 T4,
                 T5,
                 T6,
                 T7,
                 T8,
                 T9,
                 T10,
                 T11,
                 T12,
                 T13,
                 T14,
                 T15,
                 T16,
                 T17,
                 T18,
                 T19,
                 T20,
                 T21,
                 T22,
                 R]): Spore22[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              T21,
                              T22,
                              R] { type Excluded = A } =
    macro SporeTranslator.toExcludedSpore22[T1,
                                            T2,
                                            T3,
                                            T4,
                                            T5,
                                            T6,
                                            T7,
                                            T8,
                                            T9,
                                            T10,
                                            T11,
                                            T12,
                                            T13,
                                            T14,
                                            T15,
                                            T16,
                                            T17,
                                            T18,
                                            T19,
                                            T20,
                                            T21,
                                            T22,
                                            R,
                                            A]

}

object SporeTranslator {

  /** Creates a compile error if a reference for a type in the spore
    * is any of the type members of the defined `Excluded` type.
    *
    * @tparam A Single type or tuple.
    * @param s Block expression generated by the `SporeGenerator` as follows:
    *          {{{
    *          {
    *            class anonclass extends Spore...[...] {...}
    *            new anonclass(...)
    *          }
    *          }}}
    * @return  Modified expression:
    *          {{{
    *          {
    *            class anonclass extends Spore...[...] {...}
    *            new anonclass(...) { type Excluded = (..._ }
    *          }
    *          }}}
    */
  def constructTree[A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Tree): c.universe.Tree = {
    import c.universe._
    val atpe = weakTypeOf[A]

    // TODO(jvican): Increase the number of types here
    val avoidedList: List[c.universe.Type] = {
      if (atpe <:< weakTypeOf[(Any, Any)] ||
          atpe <:< weakTypeOf[(Any, Any, Any)] ||
          atpe <:< weakTypeOf[(Any, Any, Any, Any)] ||
          atpe <:< weakTypeOf[(Any, Any, Any, Any, Any)] ||
          atpe <:< weakTypeOf[(Any, Any, Any, Any, Any, Any)])
        atpe.typeArgs
      else List[c.universe.Type](atpe)
    }

    object traverser extends Traverser {
      var mentionedTypes = List[TypeTree]()
      override def traverse(tree: Tree): Unit = tree match {
        case tt @ TypeTree() => mentionedTypes = tt :: mentionedTypes
        case _ => super.traverse(tree)
      }
    }
    traverser.traverse(s)
    debug(s"${showCode(s)}")
    debug(s"Traversed: ${traverser.mentionedTypes}")

    val NothingType = typeOf[Nothing]
    /* Check that btm is indeed the
     * bottom type and that tpe is not */
    def isBottomType(btm: Type, tpe: Type) =
      btm =:= NothingType && !(tpe =:= btm)

    /* This is the check: compiler error if some TypeTree
     * in 's' has a type that is <:< of something in A */
    traverser.mentionedTypes.foreach { t =>
      avoidedList.foreach { at =>
        if (t.tpe <:< at && !isBottomType(t.tpe, at)) {
          c.abort(t.pos,
                  Feedback.InvalidReferenceToExcludedType(t.tpe.toString))
        }
      }
    }

    /* Divide the spore into pieces that are put together
     * to create a Spore[...] {type Excluded = ...} */
    val Block(stmts, newInstance) = s
    val correctFormat = stmts.headOption.exists {
      case sporeDef: ClassDef => true
      case _ => false
    }

    if (!correctFormat) c.abort(s.pos, Feedback.MissingSporeClassDef)
    else {
      val sporeDef = stmts.head
      val sporeSym = sporeDef.symbol
      val q"new ${_}(...$constructorArgs)" = newInstance
      val excludedSporeInstantiation =
        q"""
        $sporeDef
        new $sporeSym(...$constructorArgs) {type Excluded = $atpe}
      """

      debug(s"Excluded transformed spore:\n$excludedSporeInstantiation")
      excludedSporeInstantiation
    }
  }

  def toExcludedNullary[R: c.WeakTypeTag, A: c.WeakTypeTag](
      c: whitebox.Context)(s: c.Expr[NullarySpore[R]])
    : c.Expr[NullarySpore[R] { type Excluded = A }] = {
    c.Expr[NullarySpore[R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }

  def toExcludedSpore[T: c.WeakTypeTag, R: c.WeakTypeTag, A: c.WeakTypeTag](
      c: whitebox.Context)(
      s: c.Expr[Spore[T, R]]): c.Expr[Spore[T, R] { type Excluded = A }] = {
    c.Expr[Spore[T, R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }

  def toExcludedSpore2[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore2[T1, T2, R]])
    : c.Expr[Spore2[T1, T2, R] { type Excluded = A }] = {
    c.Expr[Spore2[T1, T2, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore3[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore3[T1, T2, T3, R]])
    : c.Expr[Spore3[T1, T2, T3, R] { type Excluded = A }] = {
    c.Expr[Spore3[T1, T2, T3, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore4[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore4[T1, T2, T3, T4, R]])
    : c.Expr[Spore4[T1, T2, T3, T4, R] { type Excluded = A }] = {
    c.Expr[Spore4[T1, T2, T3, T4, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore5[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       T5: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore5[T1, T2, T3, T4, T5, R]])
    : c.Expr[Spore5[T1, T2, T3, T4, T5, R] { type Excluded = A }] = {
    c.Expr[Spore5[T1, T2, T3, T4, T5, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore6[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       T5: c.WeakTypeTag,
                       T6: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore6[T1, T2, T3, T4, T5, T6, R]])
    : c.Expr[Spore6[T1, T2, T3, T4, T5, T6, R] { type Excluded = A }] = {
    c.Expr[Spore6[T1, T2, T3, T4, T5, T6, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore7[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       T5: c.WeakTypeTag,
                       T6: c.WeakTypeTag,
                       T7: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore7[T1, T2, T3, T4, T5, T6, T7, R]])
    : c.Expr[Spore7[T1, T2, T3, T4, T5, T6, T7, R] { type Excluded = A }] = {
    c.Expr[Spore7[T1, T2, T3, T4, T5, T6, T7, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }

  def toExcludedSpore8[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       T5: c.WeakTypeTag,
                       T6: c.WeakTypeTag,
                       T7: c.WeakTypeTag,
                       T8: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R]]): c.Expr[
    Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R] { type Excluded = A }] = {
    c.Expr[Spore8[T1, T2, T3, T4, T5, T6, T7, T8, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }
  def toExcludedSpore9[T1: c.WeakTypeTag,
                       T2: c.WeakTypeTag,
                       T3: c.WeakTypeTag,
                       T4: c.WeakTypeTag,
                       T5: c.WeakTypeTag,
                       T6: c.WeakTypeTag,
                       T7: c.WeakTypeTag,
                       T8: c.WeakTypeTag,
                       T9: c.WeakTypeTag,
                       R: c.WeakTypeTag,
                       A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]]): c.Expr[
    Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] { type Excluded = A }] = {
    c.Expr[Spore9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] { type Excluded = A }](
      constructTree[A](c)(s.tree))
  }
  def toExcludedSpore10[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]])
    : c.Expr[Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] {
      type Excluded = A
    }] = {
    c.Expr[Spore10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] {
      type Excluded = A
    }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore11[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]])
    : c.Expr[Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] {
      type Excluded = A
    }] = {
    c.Expr[Spore11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] {
      type Excluded = A
    }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore12[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]])
    : c.Expr[Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] {
      type Excluded = A
    }] = {
    c.Expr[Spore12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] {
      type Excluded = A
    }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore13[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]])
    : c.Expr[
      Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] {
        type Excluded = A
      }] = {
    c.Expr[Spore13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] {
      type Excluded = A
    }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore14[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore14[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                R]]): c.Expr[
    Spore14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] {
      type Excluded = A
    }] = {
    c.Expr[
      Spore14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] {
        type Excluded = A
      }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore15[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore15[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                R]]): c.Expr[
    Spore15[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore15[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore16[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore16[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                R]]): c.Expr[
    Spore16[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore16[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore17[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore17[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                R]]): c.Expr[
    Spore17[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore17[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore18[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        T18: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore18[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                T18,
                R]]): c.Expr[
    Spore18[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            T18,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore18[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              T18,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore19[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        T18: c.WeakTypeTag,
                        T19: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore19[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                T18,
                T19,
                R]]): c.Expr[
    Spore19[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            T18,
            T19,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore19[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              T18,
              T19,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore20[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        T18: c.WeakTypeTag,
                        T19: c.WeakTypeTag,
                        T20: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore20[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                T18,
                T19,
                T20,
                R]]): c.Expr[
    Spore20[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            T18,
            T19,
            T20,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore20[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              T18,
              T19,
              T20,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore21[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        T18: c.WeakTypeTag,
                        T19: c.WeakTypeTag,
                        T20: c.WeakTypeTag,
                        T21: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore21[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                T18,
                T19,
                T20,
                T21,
                R]]): c.Expr[
    Spore21[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            T18,
            T19,
            T20,
            T21,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore21[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              T18,
              T19,
              T20,
              T21,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }
  def toExcludedSpore22[T1: c.WeakTypeTag,
                        T2: c.WeakTypeTag,
                        T3: c.WeakTypeTag,
                        T4: c.WeakTypeTag,
                        T5: c.WeakTypeTag,
                        T6: c.WeakTypeTag,
                        T7: c.WeakTypeTag,
                        T8: c.WeakTypeTag,
                        T9: c.WeakTypeTag,
                        T10: c.WeakTypeTag,
                        T11: c.WeakTypeTag,
                        T12: c.WeakTypeTag,
                        T13: c.WeakTypeTag,
                        T14: c.WeakTypeTag,
                        T15: c.WeakTypeTag,
                        T16: c.WeakTypeTag,
                        T17: c.WeakTypeTag,
                        T18: c.WeakTypeTag,
                        T19: c.WeakTypeTag,
                        T20: c.WeakTypeTag,
                        T21: c.WeakTypeTag,
                        T22: c.WeakTypeTag,
                        R: c.WeakTypeTag,
                        A: c.WeakTypeTag](c: whitebox.Context)(
      s: c.Expr[
        Spore22[T1,
                T2,
                T3,
                T4,
                T5,
                T6,
                T7,
                T8,
                T9,
                T10,
                T11,
                T12,
                T13,
                T14,
                T15,
                T16,
                T17,
                T18,
                T19,
                T20,
                T21,
                T22,
                R]]): c.Expr[
    Spore22[T1,
            T2,
            T3,
            T4,
            T5,
            T6,
            T7,
            T8,
            T9,
            T10,
            T11,
            T12,
            T13,
            T14,
            T15,
            T16,
            T17,
            T18,
            T19,
            T20,
            T21,
            T22,
            R] { type Excluded = A }] = {
    c.Expr[
      Spore22[T1,
              T2,
              T3,
              T4,
              T5,
              T6,
              T7,
              T8,
              T9,
              T10,
              T11,
              T12,
              T13,
              T14,
              T15,
              T16,
              T17,
              T18,
              T19,
              T20,
              T21,
              T22,
              R] { type Excluded = A }](constructTree[A](c)(s.tree))
  }

}
