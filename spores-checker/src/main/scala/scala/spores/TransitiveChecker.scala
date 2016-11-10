package scala.spores

import scala.reflect.internal.Flags

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._

  class TransitiveTraverser(unit: CompilationUnit) extends Traverser {
    @inline def isTransient(sym: Symbol) = {
      sym.hasFlag(Flags.TRANS_FLAG) || sym.annotations.exists(
        _.tpe.typeSymbol == definitions.TransientAttr)
    }

    override def traverse(tree: Tree): Unit = tree match {
      case cls: ClassDef =>
        val members = cls.symbol.info.members
        val serializableFields = members
          .filter(m => m.isTerm && !m.isMethod && !m.isModule)
          .filterNot(isTransient)
          .toList
        val msg = s"Found in ${cls.name.decodedName}: $serializableFields"
        reporter.info(cls.pos, msg, force = true)
      case _ => super.traverse(tree)
    }
  }
}
