package scala.spores

import java.net.URLClassLoader

import scala.spores.util.PluginFeedback._

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G) {
  import global._
  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)
  val AssumeClosed = global.rootMirror.requiredClass[scala.spores.assumeClosed]
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

    def hasAnnotations(anns: List[AnnotationInfo], target: ClassSymbol) =
      anns.exists(_.tpe.typeSymbol == target)

    @inline def isTransient(sym: Symbol) = {
      hasAnnotations(sym.annotations, definitions.TransientAttr) ||
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

    @inline def nonPrimitive(sym: Symbol) =
      sym.isClass && !sym.asClass.isPrimitive

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

      /** Analyze a class hierarchy based on its symbol info and the
        * annotations that were captured in the concrete field definition. */
      def analyzeClassHierarchy(symbol: Symbol,
                                anns: List[AnnotationInfo] = Nil): Unit = {
        if (!hasAnnotations(anns, AssumeClosed)) {
          if (symbol.isSealed) {
            val subclasses = symbol.asClass.knownDirectSubclasses
            subclasses.foreach(analyzeClassHierarchy(_))
            subclasses.foreach(checkMembers(_))
          } else if (!symbol.isEffectivelyFinal) {
            val msg = openClassHierarchy(symbol.toString)
            report(config.forceClosedClassHierarchy, symbol.pos, msg)
          }
        }
      }
      def checkMembers(symbol: Symbol,
                       concreteType0: Option[Type] = None,
                       isSpore: Boolean = false): Unit = {
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

          // Spores are not final => Excluded conversion macro
          if (!isSpore && nonPrimitive(symbol)) {
            val definedAnnotations = concreteType0.map(_.annotations)
            analyzeClassHierarchy(symbol, definedAnnotations.toList.flatten)
          }

          noTransientFields.foreach { field =>
            val fieldSymbol = field.info.typeSymbol
            if (fieldSymbol.isClass) {
              if (!fieldSymbol.asClass.isPrimitive) {
                if (!field.isSerializable) reportError(field)
                else checkMembers(fieldSymbol, Some(field.tpe))
              }
            } else if (fieldSymbol.isTypeParameter) {
              val concreteType = concreteType0.getOrElse(field.tpe)
              val concreteFieldType = concreteType.memberType(fieldSymbol)
              val concreteFieldSymbol = concreteFieldType.typeSymbol

              if (nonPrimitive(concreteFieldSymbol)) {
                if (concreteFieldSymbol.isSerializable ||
                    canBeSerialized(members, concreteFieldType)) {
                  val anns = concreteFieldType.annotations
                  analyzeClassHierarchy(concreteFieldSymbol, anns)
                } else {
                  val (owner, tpe) =
                    (fieldSymbol.owner.decodedName.trim,
                     concreteFieldType.dealiasWiden.toString)
                  val msg = nonSerializableType(owner, field.toString, tpe)
                  report(true, fieldSymbol.pos, msg)
                }
              } else if (concreteFieldSymbol.isAbstractType) {
                val (symbolName, fieldName) =
                  (symbol.decodedName, fieldSymbol.decodedName)

                val hiBounds =
                  concreteFieldSymbol.typeSignature.bounds.hi.typeSymbol
                val deservesFurtherAnalysis = {
                  hiBounds.exists && !hiBounds.isAbstractType &&
                  !(hiBounds == definitions.SerializableClass ||
                    hiBounds == definitions.JavaSerializableClass)
                }
                val canBeProvedSerializable = hiBounds.isSerializable ||
                    canBeSerialized(members, hiBounds.tpe)

                if (deservesFurtherAnalysis && canBeProvedSerializable) {
                  val anns = concreteFieldSymbol.annotations
                  analyzeClassHierarchy(hiBounds, anns)
                } else if (concreteFieldSymbol.isSerializable ||
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
              report(true, field.pos, unhandledType(unhandled))
            }
          }
        }
      }

      tree match {
        case cls: ClassDef if cls.symbol.tpe <:< sporesBaseSymbol.tpe =>
          checkMembers(cls.symbol, isSpore = true)
          super.traverse(tree)
        case _ => super.traverse(tree)
      }
    }
  }
}
