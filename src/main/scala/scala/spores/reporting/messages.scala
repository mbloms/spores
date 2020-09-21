package scala.spores
package reporting

import dotty.tools.dotc._
import core._
import Contexts.Context
import reporting._
import ErrorMessageID._
import Decorators.em
import ast.tpd._
import Types._

abstract class SporeMessage extends Message(NoExplanationID) {
  def kind = "Spore"
  def explain = ""
}

class ExcludedType(typ: Type, override val explain: String)(using Context) extends SporeMessage {
  def msg = s"${typ.show} is excluded"
}

class IllegalReference(tree: Tree)(using Context) extends SporeMessage {
  def msg =
    em"""${tree.show} owned by ${tree.symbol.owner.showFullName} is not accessible in spore body."""
}
