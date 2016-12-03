package scala.spores

import java.net.URLClassLoader

import scala.spores.util.PluginFeedback._
import scala.spores.util.CheckerUtils
import scala.tools.nsc.transform.TypingTransformers

/** Check that spore captured variables are serializable transitively.
  *
  * The transitive checker is a transformer instead of a traverser because
  * it needs access to `TypingTransformers` for implicit searches. */
class TransitiveChecker[G <: scala.tools.nsc.Global](val global: G)
    extends TypingTransformers
    with CheckerUtils[G] {
  import global._

  private val classPath = global.classPath.asURLs
  val JavaClassLoader = new URLClassLoader(classPath.toArray)
  val alreadyAnalyzed = new scala.collection.mutable.HashSet[Symbol]()
  val areSerializable = new scala.collection.mutable.HashSet[Symbol]()

  /* Fetch `WeakTypeTag`s independently to avoid initialization issues. */
  object ExistentialShield {
    implicit val sporeBaseExistence = lifeVest(
      implicitly[WeakTypeTag[scala.spores.SporeBase]])
    implicit val assumeClosedExistence = lifeVest(
      implicitly[WeakTypeTag[scala.spores.assumeClosed]])
    implicit val canSerializeExistence = lifeVest(
      implicitly[WeakTypeTag[scala.spores.CanSerialize[_]]])
  }

  import ExistentialShield._
  def fromClassPath[T: WeakTypeTag] = symbolOf[T].asClass
  val sporeBaseType = fromClassPath[scala.spores.SporeBase].tpe
  val assumeClosed = fromClassPath[scala.spores.assumeClosed]
  val canSerialize = fromClassPath[scala.spores.CanSerialize[Any]]
  val deprecatedInheritance = fromClassPath[scala.deprecatedInheritance]
  val capturedSporeFieldName = "captured"

  class TransitiveTraverser(unit: CompilationUnit, config: PluginConfig)
      extends TypingTransformer(unit) {
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

    @inline def safeInferImplicit(pt: Type, c: analyzer.Context) = {
      debuglog(s"Inferring implicit of type $pt")
      c.withImplicitsEnabled {
        analyzer.inferImplicit(EmptyTree, pt, true, false, c, false)
      }
    }

    @inline
    def canBeSerialized(sym: Symbol,
                        members: Scope = new Scope {},
                        concreteType: Type = NoType) = {
      debuglog(s"Checking serializability of $sym")
      sym.isSerializable ||
      areSerializable.contains(sym.tpe.typeSymbolDirect) || {
        val evidences = members.filter(m =>
          m.isTerm && !m.isMethod && !m.isModule && m.isImplicit)
        evidences.filter { implicitEvidence =>
          val typeArgs = implicitEvidence.info.typeArgs
          implicitEvidence.tpe <:< typeOf[CanSerialize[_]] &&
          typeArgs.contains(concreteType) &&
          typeArgs.length == 1
        }.nonEmpty
      }
    }

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
      if (!canBeSerialized(symbol)) reportError(symbol)
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
        } else if (isSpore) {
          val captured = members.lookup(TermName(capturedSporeFieldName))
          if (!(captured eq NoSymbol)) {
            val capturedTpe = captured.tpe.finalResultType
            val targetTpes =
              if (!definitions.isTupleType(capturedTpe)) List(capturedTpe)
              else capturedTpe.typeArgs
            targetTpes.foreach { targetTpe =>
              val canSerializeTpe =
                canSerialize.initialize.tpe
                  .instantiateTypeParams(canSerialize.typeParams,
                                         List(targetTpe))
                  .finalResultType
              val search =
                safeInferImplicit(canSerializeTpe, localTyper.context)
              debuglog(s"Implicit search result is $search")
              if (search.isSuccess)
                areSerializable += targetTpe.typeSymbolDirect
              else debuglog(s"No `CanSerialize` implicit for $targetTpe")
            }
          }
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

        // Inspect types of the captured class
        noTransientFields.foreach { field =>
          val fieldSymbol = field.info.typeSymbol
          debuglog(s"Inspecting field of ${fieldSymbol.tpe}")
          if (fieldSymbol.isClass) {
            if (!fieldSymbol.asClass.isPrimitive) {
              if (!canBeSerialized(field)) reportError(field)
              else {
                val typeArgs = field.info.typeArgs.map(_.typeSymbol)
                debuglog(field.info.typeParams.mkString)
                if (concreteTypeArgs.isEmpty ||
                    typeArgs.nonEmpty && typeArgs.forall(!_.isTypeParameter))
                  checkMembers(fieldSymbol, Some(field.info))
                else {
                  debuglog(s"Making $typeArgs concrete with $concreteTypeArgs")
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
              if (canBeSerialized(concreteFieldSymbol,
                                  members,
                                  concreteFieldType)) {
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
              val hiCanBeSerialized =
                canBeSerialized(hiBounds, members, hiBounds.tpe)

              if (deservesFurtherAnalysis && hiCanBeSerialized) {
                debuglog(s"Symbol $hiBounds can be proven serializable")
                val as = concreteFieldSymbol.annotations
                analyzeClassHierarchy(hiBounds, as, Some(concreteFieldType))
              } else if (canBeSerialized(concreteFieldSymbol,
                                         members,
                                         concreteFieldType)) {
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

    /** Perform the transitive serializable checks from the spore header. */
    override def transform(tree: Tree): Tree = {
      tree match {
        case cls: ClassDef if cls.symbol.tpe <:< sporeBaseType =>
          debuglog(s"First target of transitive analysis: ${cls.symbol}")
          checkMembers(cls.symbol, isSpore = true)
          super.transform(tree)
          tree
        case _ =>
          super.transform(tree)
          tree
      }
    }
  }
}
