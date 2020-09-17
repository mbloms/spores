import scala.quoted._

object MacroDebug {
  inline def typeID[T](inline typ: Type[T]): Type[T] = typ
  inline def unitT: Type[Unit] = '[Unit]
  inline def debugSingle[T](inline expr: T): ${unitT} = {
   	${debugSingleImpl('expr,'[T])}
  }

  private def debugSingleImpl[T](expr: Expr[Any], typ: Type[T])(using QuoteContext): Expr[Unit] =
    '{ println("The expression " + ${Expr(expr.show)} + " evaluates to " + $expr + " and has type " + ${Expr(typ.show)} ) }
}