package scala.spores

import scala.quoted._
import scala.tasty.reflect._

transparent inline def spore[R](inline body: () => R) =
  ${sporeImpl('body)}

private def sporeImpl[R: Type](expr: Expr[() => R])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,ValDef}
  expr.unseal.underlyingArgument match
    // For now just look at the first ValDef
    case block @ Block(statements,term) => statements.head match
      case ValDef(str, tpt, Some(term)) => term.seal match
        case '{$c: $ct} =>
          '{
            new NullarySporeWithEnv[${summon[Type[R]]}] {
              override type Captured = $ct
              override val captured = $c
              override def apply() = ${expr}.apply()
              override def skipScalaSamConversion: Nothing = ???
            }
          }
      case _ => report.throwError("Only val declarations are allowed in the spore header.")
    case _ => ???
}