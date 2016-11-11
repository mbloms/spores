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
  private def isPathWith(t: Tree)(pred: TermSymbol => Boolean): Boolean = {
    t match {
      case sel @ Select(s, _) => isPathWith(s)(pred) && pred(sel.symbol.asTerm)
      case id: Ident => pred(id.symbol.asTerm)
      case th: This => true
      /* Super is not present in paths because of SI-1938 */
      // case supr: Super => true
      case _ => false
    }
  }

  private class SymbolCollector extends Traverser {
    var capturedSymbols = List.empty[(Symbol, Tree)]
    override def traverse(tree: Tree): Unit = {
      tree match {
        case app @ Apply(fun, List(captured)) if fun.symbol == captureSym =>
          debug(s"Found capture: $app")
          val culprit = captured.toString
          if (!isPathWith(captured)(_.isStable))
            ctx.abort(captured.pos, Feedback.InvalidOuterReference(culprit))
          else if (!isPathWith(captured)(!_.isLazy))
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
  private def isOwner(sym: Symbol, owner: Symbol): Boolean = {
    sym != null && (sym.owner == owner || {
      sym.owner != NoSymbol && isOwner(sym.owner, owner)
    })
  }

  /** Check whether `member` is selected from a static selector, or
    * whether its selector is transitively selected from a static symbol.
    */
  private def isStaticSelector(member: Tree): Boolean = member match {
    case Select(selector, member0) =>
      val selStatic = selector.symbol.isStatic
      debug(s"Checking whether $selector is static...$selStatic")
      selStatic || isStaticSelector(selector)
    case _ => false
  }

  private def isSymbolChildOfSpore(childSym: Symbol) =
    funSymbol.exists(sym => isOwner(childSym, sym.asInstanceOf[Symbol]))

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
  private def isSymbolValid(s: Symbol): Boolean = {
    env.contains(s) ||
    capturedSymbols.contains(s) ||
    isSymbolChildOfSpore(s) ||
    declaredSymbols.contains(s) ||
    s == NoSymbol ||
    !s.isClass && s.isStatic ||
    s.owner == definitions.PredefModule
  }

  /** Check that a path is valid by inspecting all the referred symbols. */
  private def isPathValid(tree: Tree): (Boolean, Option[Tree]) = {
    debug(s"Checking isPathValid for $tree [${tree.symbol}]...")
    debug(s" > Tree class: ${tree.getClass.getName}")
    if (tree.symbol != null && isSymbolValid(tree.symbol)) (true, None)
    else
      tree match {
        case Select(pre, sel) =>
          debug(s"Case 1: Select($pre, $sel)")
          isPathValid(pre)
        case Apply(Select(pre, _), _) =>
          debug(s"Case 2: Apply(Select($pre, _), _)")
          isPathValid(pre)
        case TypeApply(Select(pre, _), _) =>
          debug(s"Case 3: TypeApply(Select($pre, _), _)")
          isPathValid(pre)
        case TypeApply(fun, _) =>
          debug(s"Case 4: TypeApply($fun, _)")
          isPathValid(fun)
        case Literal(Constant(_)) | New(_) => (true, None)
        case id: Ident => (isSymbolValid(id.symbol), None)
        case _ =>
          debug("Case 7: _")
          (false, Some(tree))
      }
  }

  /** Inspect the body of a tree and check that all the trees in the body
    * suit the spores contract, that is, there are no invalid references to
    * non-captured symbols or external expressions like lazy vals.
    */
  private class ReferenceInspector extends Traverser {
    def checkStaticSelectOnObject(innerFun: Tree, outerSelect: Select) = {
      innerFun match {
        case Select(qual: SymTree, _) =>
          if (isSymbolChildOfSpore(qual.symbol)) {
            debug(s"OK, selected on local object $qual")
          } else {
            debug(s"Is $qual transitively selected from a top-level object?")
            val objIsStatic = qual.symbol.isStatic || isStaticSelector(qual)
            debug(s"$qual.symbol.isStatic: $objIsStatic")
            if (!objIsStatic) {
              ctx.abort(outerSelect.pos,
                        Feedback.NonStaticInvocation(innerFun.toString))
            }
          }
        case s: Select => () // Selector doesn't have a `Symbol`
        case _ => // TODO(jvican): Add test for this case
          ctx.abort(outerSelect.pos,
                    Feedback.NonStaticInvocation(innerFun.toString))
      }
    }

    override def traverse(tree: Tree): Unit = {
      tree match {
        case id: Ident =>
          debug(s"Checking ident: $id")
          if (!isSymbolValid(id.symbol))
            ctx.abort(id.pos, Feedback.InvalidReferenceTo(id.symbol.toString))
        case th: This =>
          debug(s"Checking this reference: $th")
          ctx.abort(th.pos, Feedback.InvalidReferenceTo(th.symbol.toString))
        case sp: Super =>
          debug(s"Checking super reference: $sp")
          ctx.abort(sp.pos, Feedback.InvalidReferenceTo(sp.symbol.toString))

        // x.m().s
        case sel @ Select(app @ Apply(fun0, _), _) =>
          debug(s"Checking select ($app): $sel")
          if (app.symbol.isStatic) {
            debug(s"OK, invocation of '$app' is static.")
          } else checkStaticSelectOnObject(fun0, sel)

        case sel @ Select(pre, _) =>
          debug(s"Checking select $sel")
          isPathValid(sel) match {
            case (false, Some(subtree)) => traverse(subtree)
            case (true, None) => // correct, do nothing
            case (true, Some(subtree)) => // correct, do nothing
            case (false, None) =>
              ctx.abort(sel.pos,
                        Feedback.InvalidReferenceTo(sel.symbol.toString))
          }
        case _ => super.traverse(tree)
      }
    }
  }

  def checkReferencesInBody(sporeBody: Tree) =
    (new ReferenceInspector).traverse(sporeBody)
}
