package scala.spores

import java.net.URLClassLoader

import scala.spores.util.PluginFeedback._
import scala.spores.util.CheckerUtils

class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G)
    extends CheckerUtils[G] {
  import global._

  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)
  val alreadyAnalyzed = new scala.collection.mutable.HashSet[Symbol]()

  def classOf[T: WeakTypeTag] = symbolOf[T].asClass
  val sporeBaseType = lifeVest(classOf[scala.spores.SporeBase].tpe)
  val assumeClosed = lifeVest(classOf[scala.spores.assumeClosed])
  val deprecatedInheritance = classOf[scala.deprecatedInheritance]

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

    def hasAnnotations(anns: List[AnnotationInfo], targets: ClassSymbol*) = {
      debuglog(s"Checking annotations $targets in $anns")
      anns.exists(ann => targets.contains(ann.tpe.typeSymbol))
    }

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
      def analyzeClassHierarchy(sym: Symbol,
                                anns0: List[AnnotationInfo] = Nil,
                                concreteType: Option[Type] = None,
                                typeArgs: List[Symbol] = Nil): Unit = {
        val symbol = sym.initialize
        val definitionAnns = concreteType.map(_.typeSymbol.annotations)
        val anns = (anns0 ++ definitionAnns.toList.flatten).distinct
        if (!alreadyAnalyzed.contains(symbol) &&
            !hasAnnotations(anns, assumeClosed, deprecatedInheritance)) {
          alreadyAnalyzed += symbol
          if (symbol.isSealed) {
            val subclasses = symbol.asClass.knownDirectSubclasses
            subclasses.foreach(analyzeClassHierarchy(_, anns, concreteType))
            subclasses.foreach { subclass =>
              if (subclass.typeParams.nonEmpty) {
                val concreteTypeArgs = concreteType
                  .map(_.typeArgs.map(_.typeSymbol))
                  .getOrElse(typeArgs)
                debuglog(s"Pass concrete type args... $concreteTypeArgs")
                checkMembers(subclass, concreteType, concreteTypeArgs)
              } else checkMembers(subclass, concreteType)
            }
          } else if (!symbol.isEffectivelyFinal) {
            val msg = openClassHierarchy(symbol.toString)
            report(config.forceClosedClassHierarchy, symbol.pos, msg)
          }
        }
      }
      def checkMembers(sym: Symbol,
                       concreteType0: Option[Type] = None,
                       concreteTypeArgs: List[Symbol] = Nil,
                       isSpore: Boolean = false): Unit = {
        val symbol = sym.initialize
        if (!symbol.isSerializable) reportError(symbol)
        else {
          debuglog(s"Checking member $symbol (${sym.tpe}) from $concreteType0")
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
          debuglog(s"Type params from base classes: $tparamsBaseClassMembers")

          val allMembers = (termMembers ++ tparamsBaseClassMembers).toList
          val noTransientFields = pruneScope(newScopeWith(allMembers: _*))

          // Spores are not final => Excluded conversion macro
          if (!isSpore && nonPrimitive(symbol)) {
            val definedAnns = concreteType0.map(_.annotations).toList.flatten
            analyzeClassHierarchy(symbol, definedAnns, concreteType0)
          }

          // Preparing base type args for HK type analysis
          val maybeMappedBaseTypeArgs = concreteType0.map { concreteType =>
            val concreteTypeSym = concreteType.typeSymbol
            val baseTypeArgs = symbolInfo.baseType(concreteTypeSym).typeArgs
            debuglog(s"Base type args are $baseTypeArgs")
            val concreteTypeParams = concreteTypeSym.info.typeParams
            val mapped = baseTypeArgs.zip(concreteTypeParams)
            debuglog(s"Mapped base type args are $mapped")
            mapped
          }

          noTransientFields.foreach { field =>
            val fieldSymbol = field.info.typeSymbol
            debuglog(s"Inspecting field of ${fieldSymbol.tpe}")
            if (fieldSymbol.isClass) {
              if (!fieldSymbol.asClass.isPrimitive) {
                if (!field.isSerializable) reportError(field)
                else {
                  val typeArgs = field.info.typeArgs.map(_.typeSymbol)
                  debuglog(field.info.typeParams.mkString)
                  if (concreteTypeArgs.isEmpty ||
                      typeArgs.nonEmpty && typeArgs.forall(!_.isTypeParameter))
                    checkMembers(fieldSymbol, Some(field.info))
                  else {
                    debuglog(
                      s"Making $typeArgs concrete with $concreteTypeArgs")
                    val concretized = field.tpe.instantiateTypeParams(
                      typeArgs,
                      concreteTypeArgs.map(_.tpe))
                    debuglog(s"Concrete result is $concretized")
                    checkMembers(concretized.typeSymbol, Some(concretized))
                  }
                }
              }
            } else if (fieldSymbol.isTypeParameter) {
              // Get the most concrete type possible from the defn site
              val concreteType = concreteType0.getOrElse(field.tpe)
              val concreteFieldType = maybeMappedBaseTypeArgs.map { ts =>
                val mapped = ts.find(_._1 == field.tpe).map(_._2)
                concreteType.memberType(mapped.getOrElse(fieldSymbol))
              }.getOrElse(field.tpe)
              val concreteFieldSymbol = concreteFieldType.typeSymbol
              debuglog(s"Computed $concreteFieldSymbol from $concreteType")

              if (nonPrimitive(concreteFieldSymbol)) {
                if (concreteFieldSymbol.isSerializable ||
                    canBeSerialized(members, concreteFieldType)) {
                  debuglog(s"Symbol $concreteFieldSymbol can be serialized")
                  val anns = concreteFieldType.annotations
                  analyzeClassHierarchy(concreteFieldSymbol,
                                        anns,
                                        Some(concreteFieldType))
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
                  debuglog(s"Symbol $hiBounds can be proven serializable")
                  val as = concreteFieldSymbol.annotations
                  analyzeClassHierarchy(hiBounds, as, Some(concreteFieldType))
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
          debuglog(s"Finished analysis of $symbol")
        }
      }

      tree match {
        case cls: ClassDef if cls.symbol.tpe <:< sporeBaseType =>
          debuglog(s"First target of transitive analysis: ${cls.symbol}")
          checkMembers(cls.symbol, isSpore = true)
          super.traverse(tree)
        case _ => super.traverse(tree)
      }
    }
  }
}
