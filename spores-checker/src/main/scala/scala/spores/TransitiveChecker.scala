package scala.spores

import scala.reflect.internal.Flags

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._

  class TransitiveTraverser(unit: CompilationUnit) extends Traverser {
    @inline def isTransient(sym: Symbol) = {
      sym.hasFlag(Flags.TRANS_FLAG) || sym.annotations.exists(
        _.tpe.typeSymbol == definitions.TransientAttr)
    }

    /** Transitively check that the types of the fields are Serializable.
      *
      * Watch out: traverse works by checking the fields of a given symbol.
      * However, traits only have accessors, not actual fields, and
      * accessors are ignored in the analysis. This could lead us to false
      * positives because top-level traits can be instantiated and passed
      * through the program. This is dangerous and must be checked carefully.
      */
    override def traverse(tree: Tree): Unit = {
      def checkMembers(symbol: Symbol) = {
        val members = symbol.info.members
        reporter.info(symbol.pos, s"Found members $members", force = true)
        val fields = members
          .filter(m => m.isTerm && !m.isMethod && !m.isModule)
          .filterNot(isTransient)
          .toList
        val msg = s"Fields in ${symbol.name.decodedName}: $fields"
        reporter.info(symbol.pos, msg, force = true)
        fields.foreach { field =>
          if (!field.isSerializable && !field.info.typeSymbol.asClass.isPrimitive) {
            reporter.warning(field.pos,
                             s"Not serializable: $field with type ${field.tpe}")
          }
        }
      }

      tree match {
        case cls: ClassDef => checkMembers(cls.symbol)
        case _ => super.traverse(tree)
      }
    }
  }
}
