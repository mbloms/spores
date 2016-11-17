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

    @inline def onlyTerm(member: Symbol) =
      member.isTerm && !member.isMethod && !member.isModule

    // TODO(jvican): Don't remove implicits values from the analysis
    @inline def pruneScope(members: Scope) =
      members.filter(m => !m.isImplicit && !isTransient(m))

    @inline
    def canBeSerialized(members: Scope, concreteType: Type) = {
      // TODO(jvican): Augment this with a proper implicit search
      val evidences = members.filter(m =>
        m.isTerm && !m.isMethod && !m.isModule && m.isImplicit)
      evidences.filter { implicitEvidence =>
        val typeArgs = implicitEvidence.info.typeArgs
        implicitEvidence.tpe <:< typeOf[CanBeSerialized[_]] &&
        typeArgs.contains(concreteType) &&
        typeArgs.length == 1
      }.nonEmpty
    }

    /** Transitively check that the types of the fields are Serializable. */
    override def traverse(tree: Tree): Unit = {
      def checkMembers(symbol: Symbol,
                       concreteType0: Option[Type] = None): Unit = {
        if (!symbol.isSerializable) reportError(symbol)
        else {
          val symbolInfo = symbol.info
          val members = symbolInfo.members
          val termMembers = members.filter(onlyTerm)
          val currentTypeParams = symbolInfo.typeParams.map(_.tpe)
          val numberCurrentTypeParams = currentTypeParams.length

          // Get the members of base classes with already applied type params
          val tparamsBaseClassMembers = symbolInfo.baseClasses.filter { bc =>
            val numberTypeParams = bc.typeParams.length
            numberTypeParams > 0 &&
            numberTypeParams > numberCurrentTypeParams
          }.flatMap(_.info.members.filter(onlyTerm))

          val allMembers = (termMembers ++ tparamsBaseClassMembers).toList
          val noTransientFields = pruneScope(newScopeWith(allMembers: _*))

          noTransientFields.foreach { field =>
            val fieldSymbol = field.info.typeSymbol
            if (fieldSymbol.isClass) {
              if (!fieldSymbol.asClass.isPrimitive) {
                if (!field.isSerializable) reportError(field)
                else checkMembers(field.info.typeSymbol, Some(field.tpe))
              }
            } else if (fieldSymbol.isTypeParameter) {
              val (symbolName, fieldName) =
                (symbol.decodedName, fieldSymbol.decodedName)

              // This is safe, we must have the concrete type if tparam
              val concreteType = concreteType0.get
              val concreteFieldType = concreteType.memberType(fieldSymbol)
              val concreteFieldSymbol = concreteFieldType.typeSymbol
              if (concreteFieldSymbol.isClass &&
                  !concreteFieldSymbol.asClass.isPrimitive) {
                if (concreteFieldSymbol.isSerializable ||
                    canBeSerialized(members, concreteFieldType)) {
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
                  val (owner, tpe) =
                    (fieldSymbol.owner.decodedName.trim,
                     concreteFieldType.dealiasWiden.toString)
                  val msg = nonSerializableType(owner, field.toString, tpe)
                  reporter.error(fieldSymbol.pos, msg)
                }
              } else if (concreteFieldSymbol.isAbstractType) {
                if (concreteFieldSymbol.isSerializable ||
                    canBeSerialized(members, concreteFieldType)) {
                  val concrete = Some(concreteType.toString)
                  val msg = stopInspection(symbolName, fieldName, concrete)
                  report(config.forceTransitive, field.pos, msg)
                } else {
                  val msg = nonSerializableTypeParam(symbolName, fieldName)
                  report(config.forceSerializableTypeParams, field.pos, msg)

                }
              }
            } else {
              val unhandled = fieldSymbol.tpe.toString
              reporter.error(field.pos, unhandledType(unhandled))
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
