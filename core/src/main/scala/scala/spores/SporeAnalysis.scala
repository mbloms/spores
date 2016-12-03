package scala.spores

import scala.reflect.macros.whitebox
import scala.spores.util.Feedback

protected class SporeAnalysis[C <: whitebox.Context with Singleton](val ctx: C) {

  import ctx.universe.Flag._
  import ctx.universe._

  /** Strip the header and the body of a spore iff they are valid. */
  def stripSporeStructure(tree: Tree): (List[ValDef], Tree) = {
    def isCorrectHeader(valDef: ValDef) = !valDef.mods.hasFlag(MUTABLE)

    tree match {
      case Block(stmts, expr) =>
        (stmts flatMap {
          case vd: ValDef if isCorrectHeader(vd) => List(vd)
          case stmt => ctx.abort(stmt.pos, Feedback.IncorrectSporeHeader)
        }) -> expr
      case expr => (List.empty, expr)
    }
  }

  private val SporesDefinition = typeOf[spores.`package`.type]
  private val delayedSym = SporesDefinition.member(TermName("delayed"))
  private val captureSym = SporesDefinition.member(TermName("capture"))

  /** Identify spores (nullary and non-nullary) and return their params and body. */
  def readSporeFunDef(tree: Tree): (Option[Function], List[Tree], Tree) = {
    tree match {
      case f @ Function(params, body) =>
        (Some(f), params, body) // Non-nullary
      case Apply(f, List(arg)) if f.symbol == delayedSym =>
        (None, List(), arg) // Nullary spore
      case _ => ctx.abort(tree.pos, Feedback.IncorrectSporeBody)
    }
  }

  /** Check predicate is satisfied for a concrete path. */
  private def isIdentifier(t: Tree)(pred: TermSymbol => Boolean): Boolean = {
    t match {
      case id: Ident => pred(id.symbol.asTerm)
      case _ =>
        debug(s"isPathWith fails with $t")
        false
    }
  }

  private class SymbolCollector extends Traverser {
    var capturedSymbols = List.empty[(Symbol, Tree)]
    override def traverse(tree: Tree): Unit = {
      tree match {
        case app @ Apply(fun, List(captured)) if fun.symbol == captureSym =>
          debug(s"Found capture: $app")
          val culprit = captured.toString
          if (!isIdentifier(captured)(_.isStable))
            ctx.abort(captured.pos, Feedback.InvalidOuterReference(culprit))
          else if (!isIdentifier(captured)(!_.isLazy))
            ctx.abort(captured.pos, Feedback.InvalidLazyMember(culprit))
          else capturedSymbols ::= captured.symbol -> captured

        case _ => super.traverse(tree)
      }
    }
  }

  def collectCaptured(sporeBody: Tree): List[(Symbol, Tree)] = {
    debug("Collecting captured symbols...")
    val collector = new SymbolCollector
    collector.traverse(sporeBody)
    collector.capturedSymbols
  }

  private class VariableDeclarationCollector extends Traverser {
    var declaredSymbols = List.empty[Symbol]
    override def traverse(tree: Tree): Unit = {
      tree match {
        case vd: ValDef =>
          super.traverse(tree)
          declaredSymbols ::= vd.symbol
        case _ => super.traverse(tree)
      }
    }
  }

  def collectDeclared(sporeBody: Tree): List[Symbol] = {
    debug("Collecting declared symbols...")
    val collector = new VariableDeclarationCollector
    collector.traverse(sporeBody)
    collector.declaredSymbols
  }
}

protected class SporeChecker[C <: whitebox.Context with Singleton](val ctx: C)(
    val env: List[C#Symbol],
    val funSymbol: Option[C#Symbol],
    val capturedSymbols: List[C#Symbol],
    var declaredSymbols: List[C#Symbol]) {
  import ctx.universe._

  /** Check whether the owner chain of `sym` contains `owner`.
    *
    * @param sym   the symbol to be checked
    * @param owner the owner symbol that we try to find
    * @return      whether `owner` is a direct or indirect owner of `sym`
    */
  @inline private def isOwner(sym: Symbol, owner: Symbol): Boolean = {
    sym != null && (sym.owner == owner || {
      sym.owner != NoSymbol && isOwner(sym.owner, owner)
    })
  }

  @inline private def isSymbolChildOfSpore(childSym: Symbol) =
    funSymbol.exists(sym => isOwner(childSym, sym.asInstanceOf[Symbol]))

  @inline def isStaticPath(s: Symbol): Boolean = {
    // Disclaimer: Static does not work as the reflect docs say
    s != NoSymbol && {
      (s.isMethod && isStaticPath(s.owner)) || {
        (s.isModule || s.isModuleClass || s.isPackage || s.isPackageClass) &&
        (s.isStatic || isStaticPath(s.owner))
      }
    }
  }

  /** Check the validity of symbols. Spores allow refs to symbols if:
    *
    *   1. A symbol `s` is declared in the spore header.
    *   2. A symbol `s` is captured using the `capture` syntax.
    *   3. A symbol `s` is declared within a function.
    *   4. A symbol `s` has already been declared inside the body.
    *   5. A symbol `s` is [[scala.reflect.api.Universe.NoSymbol]].
    *   6. A symbol `s` is static.
    *   7. A symbol `s` is defined within [[scala.Predef]].
    *
    * @param s Symbol of a given tree inside a spore.
    * @return Whether the symbol is valid or not.
    */
  @inline private def isSymbolValid(s: Symbol): Boolean = {
    env.contains(s) ||
    capturedSymbols.contains(s) ||
    isSymbolChildOfSpore(s) ||
    declaredSymbols.contains(s) ||
    s == NoSymbol ||
    isStaticPath(s)
  }

  /** Inspect a tree and check that all the trees in the body suit the
    * spores contract, that is, there are no invalid references to
    * non-captured symbols or external expressions like lazy vals.
    */
  private class ReferenceInspector extends Traverser {
    override def traverse(tree: Tree): Unit = {
      tree match {
        case Ident(_) | This(_) | Super(_) =>
          debug(s"Checking ident | this | super: $tree")
          if (!isSymbolValid(tree.symbol))
            ctx.abort(tree.pos,
                      Feedback.InvalidReferenceTo(tree.symbol.toString))
        case _ => super.traverse(tree)
      }
    }
  }

  def checkReferencesInBody(sporeBody: Tree) =
    (new ReferenceInspector).traverse(sporeBody)
}
