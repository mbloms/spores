package scala.spores

import java.net.URLClassLoader

import scala.spores.util.PluginFeedback._

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._
  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)
  val sporesBaseSymbol = global.rootMirror.symbolOf[scala.spores.SporeBase]
  val alreadyChecked = scala.collection.mutable.HashMap[Symbol, Boolean]()

  class TransitiveTraverser(unit: CompilationUnit, config: PluginConfig)
      extends Traverser {
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

    def reportError(sym: Symbol) = {
      val (owner, tpe) =
        (sym.owner.decodedName.trim, sym.tpe.dealiasWiden.toString)
      val msg = NonSerializableType(owner.toString, sym.toString, tpe)
      reporter.error(sym.pos, msg)
    }

    def report(errorOrWarning: Boolean, pos: Position, msg: String) = {
      if (errorOrWarning) reporter.error(pos, msg)
      else reporter.warning(pos, msg)
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
      def checkMembers(symbol: Symbol,
                       concreteType0: Option[Type] = None): Unit = {
        if (!symbol.isSerializable) reportError(symbol)
        else {
          val members = symbol.info.members
          //reporter.info(symbol.pos, s"Found members $members", force = true)
          val noTransientFields = members
            .filter(m =>
              m.isTerm && !m.isMethod && !m.isModule && !m.isImplicit)
            .filterNot(isTransient)
            .toList
          //val msg = s"Fields in ${symbol.decodedName}: $noTransientFields"
          //reporter.info(symbol.pos, msg, force = true)
          noTransientFields.foreach { field =>
            val fieldSymbol = field.info.typeSymbol
            if (fieldSymbol.isClass) {
              if (!fieldSymbol.asClass.isPrimitive) {
                if (!field.isSerializable) reportError(field)
                else checkMembers(field.info.typeSymbol, Some(field.tpe))
              }
            } else if (fieldSymbol.isTypeParameter) {
              // TODO(jvican): Improve error handling here.
              val concreteType = concreteType0.get
              val concreteFieldType = concreteType.memberType(fieldSymbol)
              val concreteFieldSymbol = concreteFieldType.typeSymbol
              if (!concreteFieldSymbol.asClass.isPrimitive) {
                val evidences = members.filter(m =>
                  m.isTerm && !m.isMethod && !m.isModule && m.isImplicit)
                val existingEvidence = evidences.filter { scope =>
                  val typeArgs = symbol.info.typeArgs
                  scope.tpe <:< typeOf[CanBeSerialized[_]] &&
                  typeArgs.contains(fieldSymbol.tpe) &&
                  typeArgs.length == 1
                }

                val (symbolName, fieldName) =
                  (symbol.decodedName, fieldSymbol.decodedName)
                if (concreteFieldSymbol.isSerializable || existingEvidence.nonEmpty) {
                  // The phantom of SI-7046 follow us...
                  val subclasses = concreteFieldSymbol.asClass.knownDirectSubclasses
                  // TODO(jvican): Check for sealed class hierarchy
                  if (subclasses.isEmpty) {
                    val msg =
                      StoppedTransitiveInspection(symbolName,
                                                  fieldName,
                                                  Some(concreteType.toString))
                    report(config.forceTransitive, field.pos, msg)
                  } else subclasses.foreach(checkMembers(_))
                } else {
                  val msg = NonSerializableTypeParam(symbolName, fieldName)
                  report(config.forceSerializableTypeParams, field.pos, msg)
                }
              }
            } else {
              reporter.error(field.pos,
                             s"Type ${fieldSymbol.tpe} is not handled.")
            }
          }
        }
      }

      tree match {
        case Block(List(cls: ClassDef), _) =>
          if (cls.symbol.tpe <:< sporesBaseSymbol.tpe) {
            checkMembers(cls.symbol)
            super.traverse(tree)
          }
        case _ => super.traverse(tree)
      }
    }
  }
}
