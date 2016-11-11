package scala.spores

import java.net.URLClassLoader

import scala.spores.util.Feedback._

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._
  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)
  val sporesBaseSymbol = global.rootMirror.symbolOf[scala.spores.SporeBase]
  val alreadyChecked = scala.collection.mutable.HashMap[Symbol, Boolean]()

  class TransitiveTraverser(unit: CompilationUnit) extends Traverser {
    @inline private def isTransientInJava(sym: Symbol): Boolean = {
      //debug(s"Checking ref is serializable: $ref")
      val className = sym.owner.asClass.fullName
      val fieldName = sym.name.decoded
      // TODO(jvican): Hack, see https://issues.scala-lang.org/browse/SI-10042
      val javaClass = JavaClassLoader.loadClass(className)
      val field = javaClass.getDeclaredField(fieldName)
      java.lang.reflect.Modifier.isTransient(field.getModifiers)
    }

    @inline def isTransient(sym: Symbol) = {
      sym.annotations.exists(_.tpe.typeSymbol == definitions.TransientAttr) ||
      (sym.isJavaDefined && isTransientInJava(sym))
    }

    @inline def reportError(sym: Symbol) = {
      val (owner, tpe) = (sym.owner.decodedName.trim, sym.tpe)
      val msg  = NonSerializableType(owner.toString, sym.toString, tpe.toString)
      reporter.error(sym.pos, msg)
    }

    /** Transitively check that the types of the fields are Serializable.
      *
      * Watch out: traverse works by checking the fields of a given symbol.
      * However, abstract fields of traits are represented as accessors, and
      * accessors are ignored in the analysis. This could lead us to false
      * positives because top-level traits can be instantiated and passed
      * through the program without any inherited class holding the fields,
      * only the accessors. This is dangerous and must be checked carefully.
      */
    override def traverse(tree: Tree): Unit = {
      def checkMembers(symbol: Symbol): Unit = {
        if (!symbol.isSerializable) reportError(symbol)
        else {
          val members = symbol.info.members
          //reporter.info(symbol.pos, s"Found members $members", force = true)
          val noTransientFields = members
            .filter(m => m.isTerm && !m.isMethod && !m.isModule)
            .filterNot(isTransient)
            .toList
          val msg =
            s"Fields in ${symbol.name.decodedName}: $noTransientFields"
          //reporter.info(symbol.pos, msg, force = true)
          noTransientFields.foreach { field =>
            if (!field.info.typeSymbol.asClass.isPrimitive) {
              if (!field.isSerializable) reportError(field)
              else checkMembers(field.info.typeSymbol)
            }
          }
        }
      }

      tree match {
        case Block(List(cls: ClassDef), invocation) =>
          if (cls.symbol.tpe <:< sporesBaseSymbol.tpe) {
            checkMembers(cls.symbol)
            super.traverse(tree)
          }
        case _ => super.traverse(tree)
      }
    }
  }
}
