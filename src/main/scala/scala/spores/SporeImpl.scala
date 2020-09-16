package scala.spores

import scala.quoted._
import scala.tasty.reflect._

transparent inline def wtf(inline expr: Any): Unit =
  ${wtfImpl('expr)}

private def wtfImpl(expr: Expr[Any])(using qctx: QuoteContext): Expr[Unit] =
  '{println(${Expr(expr.unseal.underlyingArgument.toString)})}

transparent inline def spore[R](inline body: () => R) =
  ${captured('body)}

private def nullarySporeImpl[R, C, E](body: Expr[R]): Expr[NullarySpore[R]] = ???

private def captured[R: Type](expr: Expr[() => R])(using qctx: QuoteContext) = {
  import qctx.tasty.{Block,Closure,Statement,ValDef,TypeDef}
  
  def capturedType(statements: List[Statement]) = statements match {
    case x :: xs => x match {
      case d: ValDef => d.tpt.tpe
      case _ => ???
    }
    case _ => ???
  } 
  expr.unseal.underlyingArgument match {
    case block @ Block(statements,term) => {
      '{
        {
          println("This is a block!")
          new NullarySporeWithEnv[${summon[Type[R]]}] {
            val captive =
              ${statements.head match
                case ValDef(str, tpt, Some(term)) => '{
                  println(${Expr(str)})
                  println(${Expr(tpt.show)})
                  ${term.seal}
                }
              }
            override type Captured = ${capturedType(statements)} //captive.type
            override val captured = captive
            override def apply() = {
              println("Look! I captured a " + ${Expr(capturedType(statements).seal.show)})
              println(${Expr('[Captured].show)})
              println("This is a block.")
              println("It looks like this:")
              println(${Expr(block.toString)})
              ${expr}.apply()
            }
            override def skipScalaSamConversion: Nothing = ???
          }
        }
      }
    }
    case Closure(meth,tpt) => {
      '{
      println("This is a closure!")
      new NullarySpore[${summon[Type[R]]}] {
        override def apply() = {
          println("This is a closure.")
          ${expr}.apply()
        }
        override def skipScalaSamConversion: Nothing = ???
      }}
    }
    case somethingElse => {
      '{
      println("This is something else???")
      println(${Expr(expr.show)})
      new NullarySpore[${summon[Type[R]]}] {
        override def apply() = {
          println("This is something else???")
          println(${Expr(expr.show)})
          ${expr}.apply()
        }
        override def skipScalaSamConversion: Nothing = ???
      }}
    }
  }
}