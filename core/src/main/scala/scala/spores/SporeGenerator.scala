package scala.spores

import scala.reflect.macros.whitebox

protected class SporeGenerator[C <: whitebox.Context](val ctx: C) {
  import ctx.universe._

  /** Create a tuple from trees. Note that signature is different than `createCapturedType`. */
  def toTuple(capturedTypes: Array[Tree]): Tree = {
    if (capturedTypes.length == 1) q"${capturedTypes(0)}"
    else if (capturedTypes.length == 2)
      q"(${capturedTypes(0)}, ${capturedTypes(1)})"
    else if (capturedTypes.length == 3)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)})"
    else if (capturedTypes.length == 4)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(3)})"
    else if (capturedTypes.length == 5)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)})"
    else if (capturedTypes.length == 6)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)})"
    else if (capturedTypes.length == 7)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)})"
    else if (capturedTypes.length == 8)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(7)})"
    else if (capturedTypes.length == 9)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)})"
    else if (capturedTypes.length == 10)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)})"
    else if (capturedTypes.length == 11)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)})"
    else if (capturedTypes.length == 12)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)})"
    else if (capturedTypes.length == 13)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)})"
    else if (capturedTypes.length == 14)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)})"
    else if (capturedTypes.length == 15)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)})"
    else if (capturedTypes.length == 16)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)}, ${capturedTypes(
        12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(15)})"
    else if (capturedTypes.length == 17)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)})"
    else if (capturedTypes.length == 18)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)})"
    else if (capturedTypes.length == 19)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)})"
    else if (capturedTypes.length == 20)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)}, ${capturedTypes(
        12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(15)}, ${capturedTypes(
        16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(19)})"
    else if (capturedTypes.length == 21)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(
        19)}, ${capturedTypes(20)})"
    else if (capturedTypes.length == 22)
      q"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(
        19)}, ${capturedTypes(20)}, ${capturedTypes(21)})"
    else
      ctx.abort(ctx.enclosingPosition,
                "You cannot construct a tuple of more than 22 elements.")
  }

  /** Create a type alias for `Captured` given the captured types in the spore header. */
  def createCapturedType(capturedTypes: Array[Type]): Tree = {
    if (capturedTypes.length == 1) tq"${capturedTypes(0)}"
    else if (capturedTypes.length == 2)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)})"
    else if (capturedTypes.length == 3)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)})"
    else if (capturedTypes.length == 4)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(3)})"
    else if (capturedTypes.length == 5)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)})"
    else if (capturedTypes.length == 6)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)})"
    else if (capturedTypes.length == 7)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)})"
    else if (capturedTypes.length == 8)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(7)})"
    else if (capturedTypes.length == 9)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)})"
    else if (capturedTypes.length == 10)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)})"
    else if (capturedTypes.length == 11)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)})"
    else if (capturedTypes.length == 12)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)})"
    else if (capturedTypes.length == 13)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)})"
    else if (capturedTypes.length == 14)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)})"
    else if (capturedTypes.length == 15)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)})"
    else if (capturedTypes.length == 16)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)}, ${capturedTypes(
        12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(15)})"
    else if (capturedTypes.length == 17)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)})"
    else if (capturedTypes.length == 18)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)})"
    else if (capturedTypes.length == 19)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)})"
    else if (capturedTypes.length == 20)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(11)}, ${capturedTypes(
        12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(15)}, ${capturedTypes(
        16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(19)})"
    else if (capturedTypes.length == 21)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(
        19)}, ${capturedTypes(20)})"
    else if (capturedTypes.length == 22)
      tq"(${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)}, ${capturedTypes(
        19)}, ${capturedTypes(20)}, ${capturedTypes(21)})"
    else
      ctx.abort(ctx.enclosingPosition,
                "You cannot construct a tuple of more than 22 elements.")
  }

  private val selectorForNoTuple = q"self.captured"
  def generateCapturedReferences(env: List[Symbol]): List[Tree] = {
    if (env.size == 1) List(selectorForNoTuple)
    else
      env.indices
        .map(i => TermName(s"_${i + 1}"))
        .map(selector => q"$selectorForNoTuple.$selector")
        .toList
  }

  private val paramMods = Modifiers(Flag.PARAM)
  private val paramTermName = TermName("x")
  private def generateNewParameters(syms: List[Symbol]) = {
    def defineParam(name: TermName, sym: Symbol): ValDef =
      ValDef(paramMods, name, TypeTree(sym.typeSignature), EmptyTree)
    val paramNames = syms.map(_ => ctx.freshName(paramTermName))
    val references = paramNames.map(pn => q"$pn")
    val valDefs = paramNames.zip(syms).map(t => defineParam(t._1, t._2))
    valDefs -> references
  }

  def createNewDefDef(oldParamSymbols: List[Symbol],
                      oldBody: Tree,
                      returnType: Type,
                      environment: List[Symbol] = Nil,
                      capturedRefs: List[Tree] = Nil): DefDef = {
    val (newParamDefs, newParamRefs) = generateNewParameters(oldParamSymbols)
    val oldSymbols = oldParamSymbols ::: environment
    val mapping = oldSymbols.zip(newParamRefs ::: capturedRefs).toMap
    val body = ctx.untypecheck(typeTransformerFrom(mapping)(oldBody))
    q"def apply(..$newParamDefs): $returnType = $body".asInstanceOf[DefDef]
  }

  /**  Replace all occurrences of symbols in `m` with trees in `m`, also changing the
    * 'origin' type field to fix path-dependent types.
    *
    *  Some PTT's starting with captured variables or spore parameters are not fully
    *  traversed. Their AST shows as `TypeTree`, but the ().tpe part has additional
    *  structure. Therefore, this transform adds an `().original` field. This AST is
    *  constructed by replacing `TypeName` occurrences into `nameMap(s)` where `s`
    *  is the name of the captured variable or spore parameter. For instance:
    *
    *  TypeRef(
    *    SingleType(
    *      SingleType(NoPrefix, TermName("param")),
    *      TypeName("R") --> Select(nameMap("param"), TypeName("R"))
    *    )
    *  )
    */
  private class TypeTransformer(val m: Map[Symbol, Tree]) extends Transformer {

    /** Extract the type name from a type symbol. */
    def matchTypeName(tn: Symbol): Option[TypeName] = {
      tn match {
        case TypeSymbolTag(ts) => Some(ts.name.toTypeName)
        case _ => None
      }
    }

    /** Extract the term name from a type symbol (PDT). */
    def matchTermName(tn: Symbol): Option[TermName] = {
      tn match {
        case TermSymbolTag(ts) => Some(ts.name.toTermName)
        case _ => None
      }
    }

    /** Set the original type of a `TypeTree`. */
    def setOriginal(original: Tree): TypeTree =
      internal.setOriginal(TypeTree(), original)

    /** Recursively construct the original Tree from a path-dependent type.
      *
      * @param tp Any type, typically looks like:
      *           TypeRef(
      *             SingleType(
      *               SingleType(NoPrefix, TermName("lit5_ui")),
      *               TermName("uref")),
      *             TypeName("R"),
      *             List()
      *           )
      * @return An AST like (null if param is not PDT):
      *         Select(
      *           Select(
      *             Ident(TermName("lit5_ui")),
      *             TermName("uref")),
      *           TermName("R")
      *         )
      */
    def constructOriginal(tp: Type): Option[Tree] = {
      tp match {
        case TypeRef(pre, typeSymbol, List()) =>
          debug(s"Found `TypeRef` to ${showRaw(typeSymbol)}")
          for {
            typeName <- matchTypeName(typeSymbol)
            transformedPre <- constructOriginal(pre)
          } yield Select(transformedPre, typeName)
        case SingleType(NoPrefix, typeSymbol) =>
          debug(s"Found `SingleType` to ${showRaw(typeSymbol)}")
          m.get(typeSymbol)
        case SingleType(pre, typeSymbol) =>
          debug(s"Found `SingleType` to ${showRaw(typeSymbol)}")
          for {
            typeName <- matchTermName(typeSymbol)
            transformedPre <- constructOriginal(pre)
          } yield Select(transformedPre, typeName)
        case _ => None
      }
    }

    override def transform(tree: Tree): Tree = {
      tree match {
        case Ident(_) => m.getOrElse(tree.symbol, tree)

        case tt: TypeTree if tt.original != null =>
          val transformedOriginal = super.transform(tt.original)
          super.transform(setOriginal(transformedOriginal))

        case tt: TypeTree if tt.original == null =>
          if (tt.children.isEmpty && m.keys.exists(k => tt.tpe.contains(k))) {
            debug(s"Found `TypeTree` with null original: ${showRaw(tree)}")
            debug(s" > Type: ${showRaw(tree.tpe)}")
            val transformed = constructOriginal(tree.tpe).map(setOriginal)
            transformed.getOrElse(tree)
          } else tree

        case _ => super.transform(tree)
      }
    }
  }

  private def typeTransformerFrom(m: Map[Symbol, Tree]): Tree => Tree =
    new TypeTransformer(m).transform(_: Tree)

  val nothingType = tq"scala.Nothing"
  val getName = q"this.getClass.getName"

  /** Generate a spore and instantiate based on its extracted information. */
  def generateSpore(sporeName: TypeName,
                    sporeType: Tree,
                    capturedType: List[Tree],
                    sporeBody: Tree,
                    constructorParams: List[Tree] = Nil) = {
    // CapturedType is a `List` so that quasiquotes splice it correctly
    val capturedParam = capturedType.map(t => q"val captured: $t")
    val capturedTypeDef = capturedType.headOption.getOrElse(nothingType)
    q"""
      class $sporeName(..$capturedParam) extends $sporeType { self =>
        type Captured = $capturedTypeDef
        this._className = $getName
        $sporeBody
      }
      new $sporeName(..$constructorParams)
    """
  }
}
