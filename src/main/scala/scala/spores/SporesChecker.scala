package scala.spores

import reporting._

import dotty.tools.dotc._
import core._
import Contexts.{Context,FreshContext}
import plugins._
import Phases.Phase
import ast.tpd._
import transform.MegaPhase.MiniPhase
import Decorators._
import Symbols._
import Constants.Constant
import transform._
import StdNames._
import Names.{Name,TermName}
import NameOps._
import Types._
import scala.collection.immutable._
import scala.collection.mutable.ListBuffer
import util.{Store,SourcePosition}
import Denotations._
import SymDenotations._
import Flags._

import reporting.Diagnostic
import reporting.ConsoleReporter

import annotation.unused

object SporesChecker {

  /*
   * TODO: Currently everything breaks if there is no spores.Spore
   * in current compilation run.
   */
  def sporeMethodSymbol(implicit ctx: Context): TermSymbol = {
    requiredModule("scala.spores").requiredMethod("typedSpore")
  }

  @unused
  def excludedTypeSymbol(implicit ctx: Context): TypeSymbol = {
    requiredClass("spores.Spore").requiredType("Excluded")
  }

  def scalaPredefSymbol(implicit ctx: Context): Symbol = {
    requiredPackage("scala").requiredClass("Predef$")
  }

  object excludedTypeNameFilter extends NameFilter {
    def apply(pre: Type, name: Name)(implicit ctx: Context): Boolean =
      name.isTypeName && name.toString.equals("Excluded")
  }

  object capturingWitnessTypeNameFilter extends NameFilter {
    def apply(pre: Type, name: Name)(implicit ctx: Context): Boolean =
      name.isTypeName && name.toString.equals("CapturingWitness")
  }

  def talkList[A](lst: List[A], show: A => String, comma: String, and: String): String = lst match {
    case Nil => ""
    case List(a) => show(a)
    case List(a,b) => show(a) + and + show(b)
    case x :: xs => show(x) + comma + talkList(xs,show,comma,and)
  }

  object SporeBody {
    def unapply(block: Block): Option[(DefDef,Closure)] = block match
      case Block(List(anondef: DefDef),closure @ Closure(_,Ident(cname),_))
        if (anondef.name.isAnonymousFunctionName && cname == anondef.name) =>
          Some(anondef,closure)
      case _ => None
  }
  
  object SporeBlock {
    def unapply(tree: Tree)(using Context): Option[(List[Tree],DefDef,Closure)] = tree match
      case Inlined(call,bindings,expansion) => (call,bindings) match
        case (EmptyTree,List()) => unapply(expansion)
        case _ =>
          report.error(s"Plugin error: Don't know how to handle this: ${tree.toString}")
          unapply(expansion)
      case SporeBody(anondef,closure) =>
        Some(Nil,anondef,closure)
      case Block(stats, SporeBody(anondef,closure)) =>
        Some(stats,anondef,closure)
      case Block(outer, SporeBlock(inner,anondef,closure)) =>
        Some(outer ++ inner, anondef,closure)
      case _ =>
        report.error(s"Unexpected: ${tree.show}",tree.sourcePos)
        ???
  }

  object Spore {
    def unapply(ap: Apply)(using Context): Option[Tree] = ap match {
      case Apply(TypeApply(ident,_),args)
        if ident.symbol equals sporeMethodSymbol => args match
          case List(arg) => Some(arg)
          case _ =>
            report.error(s"Expected exactly one argument, but found ${args.size}.", ap.sourcePos)
            None
      case Apply(ident,args)
        if ident.symbol equals sporeMethodSymbol =>
          report.error(s"Plugin error: No type information in spores call, are we after Erasure? ${ap.toString}",ap.sourcePos)
          None
      case _ => None
    }
  }

  object SporeContext {
    def empty = new SporeContext(Set.empty,Set.empty,None,None)
    def apply()(implicit location: Store.Location[SporeContext], ctx: Context): SporeContext = ctx.store(location)
    def update(f: SporeContext => SporeContext)(implicit location: Store.Location[SporeContext], ctx: Context): Context = {
      val current = SporeContext()
      val updated = f(current)
      if (current == updated) ctx
      else ctx.fresh.updateStore(location, updated)
    }
  }

  class SporeContext(
                      val captured: Set[Symbol],
                      private val excludedDenotations: Set[Denotation],
                      val entryPoint: Option[Denotation],
                      val capturingWitness: Option[Denotation]) {

    def this(outer: SporeContext) = this(outer.captured, outer.excludedDenotations,outer.entryPoint, outer.capturingWitness)

    def copy(
              captured: Set[Symbol] = this.captured,
              excludedDenotations: Set[Denotation] = this.excludedDenotations,
              entryPoint: Option[Denotation] = this.entryPoint,
              capturingWitness: Option[Denotation] = this.capturingWitness)
        = new SporeContext(captured,excludedDenotations,entryPoint,capturingWitness)

    def inSpore: Boolean = false

    def hasExcluded: Boolean = excludedDenotations.nonEmpty

    def excludedTypes(implicit ctx: Context): Set[Type] =
      excluded.map(_.info)

    def excluded(implicit ctx: Context): Set[Denotation] =
      excludedDenotations.map(_.current)

    def excludedMembers(tpe: Type)(implicit ctx: Context, sourcePos: SourcePosition) = for {
      memberDenot <- tpe.typeMembers
      if isExcluded(memberDenot.info)
    } yield memberDenot

    /** @param tpe Type to be compared
      * @return true if tpe is excluded
      * @note Repurposed. Used to examine type members aswell
      */
    def isExcluded(tpe: Type)(implicit ctx: Context, sourcePos: SourcePosition): Boolean = {
      var ret = false
      for {edenot <- excluded} {
        if (tpe =:= edenot.info) {
          report.log(tpe.show + " is excluded because it is the same as " + edenot.show + edenot.info.show, sourcePos)
          ret = true
        }
        else if (tpe <:< edenot.info) {
          report.log(tpe.show + " is excluded because it is a subtype of " + edenot.show + edenot.info.show, sourcePos)
          ret = true
        }
        else if (tpe <:< edenot.info.hiBound) {
          report.log(tpe.show + " is excluded because it is a subtype of " + edenot.show + "'s higher bound, " + edenot.info.hiBound.show, sourcePos)
          ret = true
        }
        else {
          if (tpe.loBound <:< edenot.info) {
            report.warning("Maybe " + tpe.show + " should have been excluded because it's lower bound, "+ tpe.loBound.show +", is a subtype of " + edenot.show + edenot.info.show, sourcePos)
          }
          if (tpe.loBound <:< edenot.info.hiBound) {
            report.warning("Maybe " + tpe.show + " should have been excluded because it's lower bound, "+ tpe.loBound.show +", is a subtype of " + edenot.show + "'s higher bound, " + edenot.info.hiBound.show, sourcePos)
          }
        }
      }
      ret
    }
    def exclude(denot: Denotation)(implicit ctx: Context): SporeContext =
      copy(excludedDenotations = this.excludedDenotations + denot)

    def capture(syms: IterableOnce[Symbol]) =
      copy(captured = this.captured ++ syms)
    def enterSporeAt(anondenot: Denotation)(implicit ctx: Context) = {
      copy(entryPoint = Some(anondenot))
    }
    def requireWitness(denot: Denotation)(implicit ctx: Context): SporeContext =
      copy(capturingWitness = Some(denot))

    def enter(tree: Tree)(implicit ctx: Context) =
      if (entryPoint.exists(_.symbol == tree.symbol)) {
        report.log("Entering spore...", tree.sourcePos)
        new InsideSporeContext(this)
      }
      else
        this
  }

  class InsideSporeContext(outer: SporeContext) extends SporeContext(outer) {
    override def inSpore = true
  }
}

class SporesChecker extends PluginPhase with StandardPlugin {
  import SporesChecker._
  import SporeContext._

  val name: String = "sporesChecker"
  override val description: String = """For each call spore(arg) we check two things:
1. "arg" must be a function literal, so the function must be defined in-line (e.g., it cannot be a variable name that refers to a function); and
2. the "arg" function literal captures only variables (vals or vars) whose type is on a whitelist."""

  val phaseName = name

  override val runsAfter = Set(FirstTransform.name)
  override val runsBefore = Set(CompleteJavaEnums.name,Erasure.name)

  override def init(options: List[String]): List[PluginPhase] = this :: Nil

  private implicit var sporectxloc: Store.Location[SporeContext] = _
  override def initContext(ctx: FreshContext) = {
    sporectxloc = ctx.addLocation[SporeContext](SporeContext.empty)
    
    /** Using a custom reporter screws up exit codes */
    ctx.reporter match
      case outerReporter: ConsoleReporter if !outerReporter.hasErrors =>
        ctx.setReporter(new ConsoleReporter {
          override def errorCount: Int =
            super.errorCount + outerReporter.errorCount
          override def warningCount: Int =
            super.warningCount + outerReporter.warningCount
          override def isHidden(dia: Diagnostic)(using Context): Boolean =
            super.isHidden(dia) || outerReporter.isHidden(dia)

          override def allErrors =
            super.allErrors ++ outerReporter.allErrors
            
          override def isReportedFeatureUseSite(featureTrait: Symbol): Boolean =
            outerReporter.isReportedFeatureUseSite(featureTrait)
          override def reportNewFeatureUseSite(featureTrait: Symbol): Unit =
            outerReporter.reportNewFeatureUseSite(featureTrait)

          unreportedWarnings = outerReporter.unreportedWarnings
          
          // THIS STILL EXITS WITH 0. WHY??
          override def hasErrors = true
          override def errorsReported: Boolean = true

          /** Normally, if code from source A is inlined in a call to B which is then inlined into A,
           *  the stack of inlined sources is printed, despite the fact that the code originated from source A.
           *  This implementation gets rid of that behaviour.
           */
          override def outer(pos: SourcePosition, prefix: String)(using Context): List[String] = {
            if (!pos.outermost.contains(pos) && pos.outer.exists)
              i"$prefix| This location contains code that was inlined from $pos" ::
                outer(pos.outer, prefix)
            else Nil
          }
        })
      case _ => ???
  }


  override def prepareForApply(tree: Apply)(implicit ctx: Context): Context = {
    //report.log("Apply Node: " + tree.show,tree.sourcePos)
    tree match {
      case Spore(SporeBlock(stats,anondef,closure)) =>
        val sporeType = tree.typeOpt
        val capturedVars: List[Denotation] = stats.collect({(stat: Tree) => stat match
          case v @ ValDef(name,tpt,_) =>
            if v.mods.is(Lazy) then {
              report.error("Incorrect spore header: lazy val not allowed.", v.sourcePos)
              None
            }
            else if v.mods.is(Mutable) then {
              report.error("Incorrect spore header: var not allowed.",v.sourcePos)
              None
            } else {
              report.log(s"Captured ${v.rhs.show} as ${name.show}: ${tpt.show}",v.sourcePos)
              Some(v.denot)
            }
          //case d: NamedDefTree => report.error("Incorrect spore header: Only val defs allowed at this position.",
          //                                     d.sourcePos.withSpan(d.sourcePos.span.withEnd(d.nameSpan.start-1)))
          case stat =>
            report.error("Incorrect spore header: Only val defs allowed at this position.", stat.sourcePos)
            None
        }.unlift)
        val optionExcludedDenotation = {
          /** Ignore `Excluded = Any` */
          def notAny(excludedDenotation: Denotation): Boolean =
            !excludedDenotation.info.bounds.contains(requiredModule("scala").requiredType("Any").typeRef)
          for {
            // name of spore.Excluded
            excludedName <- sporeType.memberNames(excludedTypeNameFilter).headOption
            // denotation of spore.Excluded
            excludedDenotation = sporeType.member(excludedName)
            // filter out Excluded = Any
            if notAny(excludedDenotation)
          } yield excludedDenotation
        }
        val optionWitnessDenotation = for {
          // name of spore.CapturingWitness
          witnessName <- sporeType.memberNames(capturingWitnessTypeNameFilter).headOption
          // denotation of spore.Excluded
        } yield sporeType.member(witnessName)

        //Check excluded
        for {
          captured <- capturedVars
          capturedType <- captured.info +: captured.info.typeMembers.map(_.info)
          excluded <- optionExcludedDenotation
          excludedType = excluded.info
        }
          if capturedType =:= excludedType then
            report.error(ExcludedType(capturedType, s"It is the same as ${excluded.show} ${excludedType.show}."), captured.symbol.sourcePos)
          else if capturedType <:< excludedType then
            report.error(ExcludedType(capturedType, s"It is a subtype of ${excluded.show} ${excludedType.show}"), captured.symbol.sourcePos)
          else if capturedType <:< excludedType.hiBound then
            report.error(ExcludedType(capturedType, s"It is a subtype of ${excluded.show}'s higher bound, ${excludedType.hiBound.show}."), captured.symbol.sourcePos)
          else {
            if (capturedType.loBound <:< excludedType) {
              report.warning(
                s"""|Maybe ${capturedType.show} should have been excluded. It's lower bound, ${capturedType.loBound.show},
                    |is a subtype of ${excluded.show} ${excludedType.show}""", captured.symbol.sourcePos)
            }
            if (capturedType.loBound <:< excludedType.hiBound) {
              report.warning(
                s"""|Maybe ${capturedType.show} should have been excluded. It's lower bound, ${capturedType.loBound.show},
                    |is a subtype of ${excluded.show}'s higher bound, ${excludedType.hiBound.show}""", captured.symbol.sourcePos)
            }
          }

        // Check Capturing Witness
        for {
          captured <- capturedVars
          capturedType = captured.info
          witness <- optionWitnessDenotation
          witnessType = witness.info
        } {
          val bounds = witnessType.asInstanceOf[TypeBounds]
          val lambda = bounds.underlying.asInstanceOf[HKTypeLambda]
          val res = lambda.resType

          val current = capturedType.widenTermRefExpr

          if (!(current.typeConstructor <:< res.typeConstructor)) {
          report.error("This spore only allows capturing values wrapped in a " + res.typeConstructor.show + ". "
                      + "Consider capturing it as a " + AppliedType(lambda, List(tree.typeOpt.widen)).show + " instead.", tree.sourcePos)
          }
        }
        SporeContext.update {sx =>
          var newSX = sx
          newSX = newSX.capture(capturedVars.map(_.symbol))
          if (optionWitnessDenotation.isDefined)
            newSX = newSX.requireWitness(optionWitnessDenotation.get)
          if (optionExcludedDenotation.isDefined)
            newSX = newSX.exclude(optionExcludedDenotation.get)
          newSX.enterSporeAt(anondef.denot)
        }
      case _ => ctx
    }
  }

  /** Set SporeContext().inSpore to true if tree is the function litteral */
  override def prepareForDefDef(tree: DefDef)(implicit ctx: Context): Context =
    SporeContext.update(_.enter(tree))

  override def prepareForIdent(tree: Ident)(implicit ctx: Context): Context = {
    implicit val sourcePos: SourcePosition = tree.sourcePos
    if (SporeContext().inSpore) {
      if tree.symbol.owner.denot == scalaPredefSymbol.denot
        then report.log("Capturing scala.Predef is currently allowed.", tree.sourcePos)
      else if tree.symbol.ownersIterator.contains(SporeContext().entryPoint.get.current.symbol)
        then report.log(s"Owned by spore body.",tree.sourcePos)
      else if SporeContext().captured.map(_.denot.current).contains(tree.symbol.denot.current)
        then report.log(s"Defined in header.",tree.sourcePos)
      else {
        report.error(IllegalReference(tree),tree.sourcePos)
      }
    }
    ctx
  }
}
