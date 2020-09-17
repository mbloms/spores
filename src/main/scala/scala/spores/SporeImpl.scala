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

private def union[A,B](a: A, b: B): a.type | b.type = ???

private def sporeImpl[R: Type](expr: Expr[() => R])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,ValDef,Statement}
  def capturedTypes(statements: List[Statement]): Expr[?] = statements match
    case List(statement) => statement match
      case ValDef(str, tpt, Some(term)) => term.seal match
        case '{$c: $ct} => c
      case _ => ???
    case x::xs => x match
      case ValDef(str, tpt, Some(term)) => term.seal match
        case '{$c: $ct} => '{union($c,${capturedTypes(xs)})}
    case _ => ???
  expr.unseal.underlyingArgument match
    // For now just look at the first ValDef
    case block @ Block(statements,term) => capturedTypes(statements) match
      case '{$c: $ct} =>
        '{typedSpore[${summon[Type[R]]},$ct]($expr)}
      case _ => report.throwError("Only val declarations are allowed in the spore header.")
    case _ => ???
}