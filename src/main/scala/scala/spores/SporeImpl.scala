package scala.spores

import scala.quoted._
import scala.tasty.reflect._
import scalaShadowing.language.experimental.dependent

transparent inline def spore[R](inline body: () => R) =
  ${sporeImpl('body)}

private def typedSpore[R,C](body: () => R) =
  new NullarySpore[R] {
    override type Captured = C
    //override val captured = null
    override def apply() = body()
    override def skipScalaSamConversion: Nothing = ???
  }

/** dummy function only for generating union types */
private def union(a: Any, b: Any): a.type | b.type = ???

private def sporeImpl[R: Type](expr: Expr[() => R])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,ValDef,Statement}

  /** Extract rhs term from val declaration */
  transparent inline def rhs(statement: Statement) = statement match
    case ValDef(str, tpt, Some(term)) => term.seal
    case _ => report.throwError("Only val declarations are allowed in the spore header.")

  /** Generate a bogus expression with the type of all captured variables
   *  NOTE: Currently the declared type in val defs are ignored, only the inferred type is used
   */
  def capturedTypes(statements: List[Statement]): Expr[?] = statements match {
    case List(statement) => rhs(statement) match
      case '{$c: $ct} => c
    case x::xs => rhs(x) match
      case '{$c: $ct} => '{union($c,${capturedTypes(xs)})}
  }

  expr.unseal.underlyingArgument match
    case block @ Block(statements,term) => capturedTypes(statements) match
      case '{$c: $ct} =>
        '{typedSpore[${summon[Type[R]]},$ct]($expr)}
    case _ => ???
}