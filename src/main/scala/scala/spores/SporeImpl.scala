package scala.spores

import scala.quoted._
import scala.tasty.reflect._
import scalaShadowing.language.experimental.dependent

transparent inline def spore[R,E,W[_]](inline body: () => R) =
  ${sporeImpl('body,'[E],'[W])}

transparent inline def spore[A,R,E,W[_]](inline body: A => R) =
  ${sporeImpl1('body,'[E],'[W])}

private def typedSpore[R,C,E,W[_]](body: () => R) =
  new NullarySpore[R] {
    type Captured = C
    type Excluded = E
    type CapturingWitness[T] = W[T]
    override def apply() = body()
    override def skipScalaSamConversion: Nothing = ???
  }

private def typedSpore[A,R,C,E,W[_]](body: A => R) =
  new Spore[A,R] {
    type Captured = C
    type Excluded = E
    type CapturingWitness[T] = W[T]
    override def apply(x: A) = body(x)
    override def skipScalaSamConversion: Nothing = ???
  }

/** dummy function only for generating union types */
private def union(a: Any, b: Any): a.type | b.type = ???

private def sporeImpl[R: Type, E: Type, W[_]: Type](expr: Expr[() => R], excluded: Type[E], w: Type[W])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,ValDef,Statement,error}

  /** Extract rhs term from val declaration */
  transparent inline def rhs(statement: Statement) = statement match
    case ValDef(str, tpt, Some(term)) =>
      term.seal
    case _ =>
      // Currently, error reporting is implemented in the plugin
      //error("Only val declarations are allowed in the spore header.",statement.pos)
      '{???}

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
        '{typedSpore[${summon[Type[R]]},$ct,${summon[Type[E]]},${summon[Type[W]]}]($expr)}
    case _ => '{typedSpore($expr)}
}


private def sporeImpl1[A: Type, R: Type, E: Type, W[_]: Type](expr: Expr[A => R], excluded: Type[E], w: Type[W])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,ValDef,Statement,error}

  /** Extract rhs term from val declaration */
  transparent inline def rhs(statement: Statement) = statement match
    case ValDef(str, tpt, Some(term)) =>
      term.seal
    case _ =>
      // Currently, error reporting is implemented in the plugin
      //error("Only val declarations are allowed in the spore header.",statement.pos)
      '{???}

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
        '{typedSpore[${summon[Type[A]]},${summon[Type[R]]},$ct,${summon[Type[E]]},${summon[Type[W]]}]($expr)}
    case _ => '{typedSpore($expr)}
}