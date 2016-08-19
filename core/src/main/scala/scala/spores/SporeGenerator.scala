package scala.spores

import scala.reflect.macros.whitebox

protected class SporeGenerator[C <: whitebox.Context with Singleton](
    val ctx: C) {
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
    if (capturedTypes.length == 1) q"type Captured = ${capturedTypes(0)}"
    else if (capturedTypes.length == 2)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)})"
    else if (capturedTypes.length == 3)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)})"
    else if (capturedTypes.length == 4)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)})"
    else if (capturedTypes.length == 5)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)})"
    else if (capturedTypes.length == 6)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)})"
    else if (capturedTypes.length == 7)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)})"
    else if (capturedTypes.length == 8)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)})"
    else if (capturedTypes.length == 9)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)})"
    else if (capturedTypes.length == 10)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)})"
    else if (capturedTypes.length == 11)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)})"
    else if (capturedTypes.length == 12)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)})"
    else if (capturedTypes.length == 13)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)})"
    else if (capturedTypes.length == 14)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)})"
    else if (capturedTypes.length == 15)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)})"
    else if (capturedTypes.length == 16)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)})"
    else if (capturedTypes.length == 17)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)})"
    else if (capturedTypes.length == 18)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)})"
    else if (capturedTypes.length == 19)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)})"
    else if (capturedTypes.length == 20)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)})"
    else if (capturedTypes.length == 21)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)}, ${capturedTypes(20)})"
    else if (capturedTypes.length == 22)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)}, ${capturedTypes(20)}, ${capturedTypes(21)})"
    else
      ctx.abort(ctx.enclosingPosition,
                "You cannot construct a tuple of more than 22 elements.")
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
    val body = ctx.untypecheck(transformTypes(mapping)(oldBody))
    q"def apply(..$newParamDefs): $returnType = $body".asInstanceOf[DefDef]
  }

  /**  Constructs a function that replaces all occurrences of symbols in m with trees in m and that changes the 'origin'
    *  field to fix path-dependent types.
    *  In some cases, PTT:s that start with captured variables or the spore parameters are not traversed fully.
    *  The syntax tree part of these PTTs shows as "TypeTree()", but the ().tpe part has additional structure.
    *  The 'TypeTree' case transforms these types by adding an "().original" field, that is a syntax tree.
    *  The syntax tree is constructed by replacing all TypeName(s) occurances where s is the name of a captured
    *  variable or a parameter into nameMap(s). E.g.
    *  TypeRef(SingleType(SingleType(NoPrefix, TermName("param")), TypeName("R") --> Select(nameMap("param"), TypeName("R"))
    */
  def transformTypes(m: Map[Symbol, Tree]): Tree => Tree = {
    class TypeTransformer(
        val m: Map[Symbol, Tree] //,
        //val nameMap: Map[c.universe.Symbol, Tree]
    ) extends Transformer {
      override def transform(tree: Tree): Tree = {

        tree match {
          case Ident(_) => m.getOrElse(tree.symbol, tree)
          case tt: TypeTree if tt.original != null =>
            super.transform(
              internal.setOriginal(TypeTree(), super.transform(tt.original)))
          case tt: TypeTree if tt.original == null =>
            if (tt.children.isEmpty &&
                m.keys.exists(key => tt.tpe.contains(key))) {
              debug(s"${showRaw(tree)}")
              debug(s"${showRaw(tree.tpe)}")
              debug(s"${tree.tpe}")

              /**
                * Recursively construct a Tree from a Type
                * @param tp
                *           Any type, typically looks like this:
                *           TypeRef(
                *              SingleType(
                *                 SingleType(NoPrefix, TermName("lit5_ui")),
                *                 TermName("uref")),
                *              TypeName("R"),
                *              List())
                * @return
                *         A syntax tree constructed from the type. The example would return
                *         Select(
                *            Select(
                *               Ident(TermName("lit5_ui")),
                *               TermName("uref")),
                *            TermName("R"))
                *         Returns null if the param is not a path-dependent type
                */
              def constructOriginal(tp: Type): Tree = {
                def matchTypeName(tn: Symbol): TypeName = {
                  tn match {
                    case TypeSymbolTag(ts) => {
                      ts.name.toTypeName
                    }
                    case _ => null
                  }
                }
                def matchTermNameNoPrefixCase(tn: Symbol): Tree =
                  m.getOrElse(tn, null)

                def matchTermName(tn: Symbol): TermName = {
                  tn match {
                    case TermSymbolTag(ts) => ts.name.toTermName
                    case _ => null
                  }
                }
                tp match {
                  case TypeRef(tr, tns, List()) =>
                    debug(s"tns = $tns,\nshowRaw(tns) = ${showRaw(tns)}")
                    val tnsTypeName = matchTypeName(tns)
                    if (tnsTypeName != null) {
                      val tr_rec = constructOriginal(tr)
                      if (tr_rec != null) Select(tr_rec, tnsTypeName)
                      else null
                    } else null
                  case SingleType(NoPrefix, tns) =>
                    matchTermNameNoPrefixCase(tns)
                  case SingleType(pre, tns) =>
                    val tnsTypeName = matchTermName(tns)
                    val pre_rec = constructOriginal(pre)
                    if (pre_rec != null) Select(pre_rec, tnsTypeName)
                    else null
                  case _ => null
                }
              }

              val new_orig = constructOriginal(tree.tpe)
              val res =
                if (new_orig != null)
                  internal.setOriginal(TypeTree(), new_orig)
                else
                  tree
              res
            } else tree
          case _ => super.transform(tree)
        }
      }
    }
    new TypeTransformer(m).transform(_: Tree)
  }

}
