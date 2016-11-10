package scala.spores

import java.net.URLClassLoader

import scala.reflect.internal.Flags

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._
  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)

  class TransitiveTraverser(unit: CompilationUnit) extends Traverser {

    @inline private def isTransientInJava(className: String,
                                          fieldName: String): Boolean = {
      // TODO(jvican): This is a hack, see https://issues.scala-lang.org/browse/SI-10042
      val javaClass = JavaClassLoader.loadClass(className)
      val field = javaClass.getDeclaredField(fieldName)
      java.lang.reflect.Modifier.isTransient(field.getModifiers)
    }

    @inline def isTransient(sym: Symbol) = {
      (sym.isJavaDefined && isTransientInJava(sym.owner.asClass.fullName,
                                              sym.name.decoded)) ||
      sym.hasFlag(Flags.TRANS_FLAG) ||
      sym.annotations.exists(_.tpe.typeSymbol == definitions.TransientAttr)
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
      def checkMembers(symbol: Symbol): Unit = {
        val members = symbol.info.members
        //reporter.info(symbol.pos, s"Found members $members", force = true)
        val noTransientFields = members
          .filter(m => m.isTerm && !m.isMethod && !m.isModule)
          .filterNot(isTransient)
          .toList
        val msg = s"Fields in ${symbol.name.decodedName}: $noTransientFields"
        reporter.info(symbol.pos, msg, force = true)
        noTransientFields.foreach { field =>
          if (!field.info.typeSymbol.asClass.isPrimitive) {
            if (!field.isSerializable) {
              reporter.warning(
                field.pos,
                s"Not serializable: $field with type ${field.tpe}")
            } else checkMembers(field.info.typeSymbol)
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
