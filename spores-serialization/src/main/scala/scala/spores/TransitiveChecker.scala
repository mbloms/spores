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
      val msg = nonSerializableType(owner.toString, sym.toString, tpe)
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
          // TODO(jvican): Don't remove implicits values from the analysis
          val noTransientFields = members.filter { m =>
            val validField = m.isTerm && !m.isMethod && !m.isModule
            validField && !m.isImplicit && !isTransient(m)
          }.toList

          noTransientFields.foreach { field =>
            val fieldSymbol = field.info.typeSymbol
            if (fieldSymbol.isClass) {
              if (!fieldSymbol.asClass.isPrimitive) {
                if (!field.isSerializable) reportError(field)
                else checkMembers(field.info.typeSymbol, Some(field.tpe))
              }
            } else if (fieldSymbol.isTypeParameter) {
              // This is safe, we must have the concrete type if tparam
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
                if (concreteFieldSymbol.isSerializable ||
                    existingEvidence.nonEmpty) {
                  if (concreteFieldSymbol.isSealed &&
                      !concreteFieldSymbol.isEffectivelyFinal) {
                    val subclasses =
                      concreteFieldSymbol.asClass.knownDirectSubclasses
                    subclasses.foreach(checkMembers(_))
                  } else {
                    val concrete = Some(concreteType.toString)
                    val msg = stopInspection(symbolName, fieldName, concrete)
                    report(config.forceTransitive, field.pos, msg)
                  }
                } else {
                  val msg = nonSerializableTypeParam(symbolName, fieldName)
                  report(config.forceSerializableTypeParams, field.pos, msg)
                }
              }
            } else {
              val unhandled = fieldSymbol.tpe.toString
              reporter.error(field.pos, unhandled)
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
