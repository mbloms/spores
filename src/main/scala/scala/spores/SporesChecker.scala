package scala.spores

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

  object Spore {
    def unapply(ap: Apply)(implicit ctx: Context): Option[(Block,Denotation,Option[Denotation],Option[Denotation])] = {

      object AnonFun {
        /** resugar function literal */
        def unapply(block: Block): Option[(DefDef,Closure)] = block match {
          case Block(stats,block: Block) =>
            for {
              // Is it a val declaration?
              stat <- stats
            } {
              stat match
                //TODO: Double check that tpt is included in Captured.
                case v @ ValDef(name,tpt,_) =>
                  if v.mods.is(Lazy)
                    report.error("Incorrect spore header: lazy val not allowed.", v.sourcePos)
                  if v.mods.is(Mutable)
                    report.error("Incorrect spore header: var not allowed.",v.sourcePos)
                  else
                    report.log(s"Captured ${v.rhs.show} as ${name.show}: ${tpt.show}",v.sourcePos)
                //case d: NamedDefTree => report.error("Incorrect spore header: Only val defs allowed at this position.",
                //                                     d.sourcePos.withSpan(d.sourcePos.span.withEnd(d.nameSpan.start-1)))
                case _ => report.error("Incorrect spore header: Only val defs allowed at this position.", stat.sourcePos)
            }
            unapply(block)

          case Block(List(anondef: DefDef),closure @ Closure(_,Ident(cname),_))
                if (anondef.name.isAnonymousFunctionName
                 && cname == anondef.name) =>
                  //ctx.log("This is expanded into this: " + anondef.show, anondef.sourcePos)
                  //ctx.log("This is expanded into this: " + closure.show, closure.sourcePos)
                  Some(anondef,closure)
          case _ => None
        }
      }

      /** Simplify node */
      def peel(tree: Tree): Tree = tree match
        case Block(List(),term) => peel(term)
        case Inlined(call,bindings,expansion) => (call,bindings) match
          case (EmptyTree,List()) => peel(expansion)
          case _ =>
            report.error(s"Plugin error: Don't know how to handle this: ${tree.toString}")
            peel(expansion)
        case _ => tree

      ap match {
        case Apply(TypeApply(ident,_),args)
          if ident.symbol equals sporeMethodSymbol => args match {
            case Nil =>
              report.error("Expected exactly one argument, but found none.",ap.sourcePos)
              None
            case List(arg) => peel(arg) match
              case block @ AnonFun(anondef,closure) =>

                /** The type of the Spore, the type checker will make sure this is a subtype of the expected type.*/
                val sporeType = ap.typeOpt

                /** Ignore `Excluded = Any` */
                def notAny(excludedDenotation: Denotation): Boolean =
                  if (excludedDenotation.info.bounds.contains(requiredModule("scala").requiredType("Any").typeRef)) {
                        report.warning("Ignoring excluding Any: {" + excludedDenotation.show + excludedDenotation.info.show + "}",ap.sourcePos)
                        false
                  } else true

                Some(block, anondef.denot,
                  for {
                    // name of spore.Excluded
                    excludedName <- sporeType.memberNames(excludedTypeNameFilter).headOption
                    // denotation of spore.Excluded
                    excludedDenotation = sporeType.member(excludedName)
                    // filter out Excluded = Any
                    if notAny(excludedDenotation)
                    } yield excludedDenotation,
                    (for {
                    // name of spore.CapturingWitness
                    witnessName <- sporeType.memberNames(capturingWitnessTypeNameFilter)
                    // denotation of spore.Excluded
                    witnessDenotation = sporeType.member(witnessName)
                    } yield witnessDenotation).headOption
                    )
              case arg =>
                report.error(s"Expected function litteral, but found a ${arg.getClass.toString} node.", arg.sourcePos)
                report.log(arg.toString,arg.sourcePos)
                None
            case lst =>
              report.error("Expected exactly one argument, but found " + lst.size + ": " + lst.map(_.showSummary).mkString(",\n"),lst.head.sourcePos)
              None
            }
        case Apply(ident,args)
          if ident.symbol equals sporeMethodSymbol =>
            report.error(s"Plugin error: No type information in spores call, are after Erasure? ${ap.toString}",ap.sourcePos)
            None
        case _ =>
          //report.warning(s"Not a spore: ${ap.toString}",ap.sourcePos)
          None
      }
    }
  }

  object SporeContext {
    def empty = new SporeContext(Set.empty,None,None)
    def apply()(implicit location: Store.Location[SporeContext], ctx: Context): SporeContext = ctx.store(location)
    def update(f: SporeContext => SporeContext)(implicit location: Store.Location[SporeContext], ctx: Context): Context = {
      val current = SporeContext()
      val updated = f(current)
      if (current == updated) ctx
      else ctx.fresh.updateStore(location, updated)
    }
  }

  class SporeContext(
    private val excludedDenotations: Set[Denotation],
    val entryPoint: Option[Denotation],
    val capturingWitness: Option[Denotation]) {

    def this(outer: SporeContext) = this(outer.excludedDenotations,outer.entryPoint, outer.capturingWitness)

    def copy(excludedDenotations: Set[Denotation] = this.excludedDenotations,
          entryPoint: Option[Denotation] = this.entryPoint,
          capturingWitness: Option[Denotation] = this.capturingWitness)
        = new SporeContext(excludedDenotations,entryPoint,capturingWitness)

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
        else if (tpe.loBound.isBottomType) {
          //ctx.debugwarn(tpe.show + " has lower bound " + tpe.loBound.show,sourcePos)
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
    def exclude(denot: Denotation)(implicit ctx: Context, sourcePos: SourcePosition): SporeContext =
      copy(excludedDenotations = this.excludedDenotations + denot)
    def enterSporeAt(anondenot: Denotation)(implicit ctx: Context) = {
      copy(entryPoint = Some(anondenot))
    }
    def requireWitness(denot: Denotation)(implicit ctx: Context, sourcePos: SourcePosition): SporeContext =
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
    ctx.setReporter(new ConsoleReporter {
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
  }


  override def prepareForApply(tree: Apply)(implicit ctx: Context): Context = {
    //report.log("Apply Node: " + tree.show,tree.sourcePos)
    tree match {
      case Spore(block,anondenot,Some(excludedDenotation),optionWitnessDenotation) => {
        implicit val sourcePos: SourcePosition = tree.sourcePos
        report.log("Spore: " + tree.typeOpt.show,tree.sourcePos)
        //ctx.log("Witness: " + optionWitnessDenotation.map(_.show))
        val maybeWitness: SporeContext => SporeContext =
          if (optionWitnessDenotation.isDefined)
            _.requireWitness(optionWitnessDenotation.get)
          else
            identity
        SporeContext.update {maybeWitness(_)
          .exclude(excludedDenotation)
          .enterSporeAt(anondenot)
        }
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
      //ctx.log("Ident Owner:     " + tree.symbol.ownersIterator.toList.map(_.showFullName), tree.sourcePos)
      //ctx.log("Effective Owner: " + tree.symbol.effectiveOwner.showFullName)
      //for {
      //  identOwner <- tree.symbol.ownersIterator.toList
      //  sporeOwner <- SporeContext().entryPoint.get.current.symbol.ownersIterator.toList
      //} if (identOwner == sporeOwner)
      //    then ctx.log(identOwner.showFullName + " == " + sporeOwner.showFullName)
      //    else ctx.log(identOwner.showFullName + " != " + sporeOwner.showFullName)

      if (SporeContext().hasExcluded) {

        //val witness = SporeContext().capturingWitness
        //if witness.isDefined
        //ctx.log("CapturingWitness: " + witness.map(_.show))
        //if (!tree.symbol.owner != SporeContext())
        tree.typeOpt match
          case NoType => report.error("Identifier has no type",tree.sourcePos)
          case typ =>
            if (SporeContext().isExcluded(typ))
              report.error(tree.symbol.showDcl + " is excluded", tree.sourcePos)
            else SporeContext().excludedMembers(typ) match
              case es if es.nonEmpty =>
                for {excludedMember <- es}
                report.error(tree.symbol.showDcl + " has an excluded member "
                      + excludedMember.show + excludedMember.info.show,
                      tree.sourcePos)
              case _ =>
                // change to ctx.log to make visible
                report.inform(typ.show + " is not excluded. (unlike: " + talkList(SporeContext().excludedTypes.toList,(_.hiBound.show),", "," and ") + ")",tree.sourcePos)
      }
      // Skip checking if definition is local
      if (SporeContext().capturingWitness.isDefined) {
        if (tree.symbol.owner == SporeContext().entryPoint.get.current.symbol) {
          report.log("This is a local definition, not a captured variable and therefore ok.", tree.sourcePos)
        } else {
          val bounds = SporeContext().capturingWitness.get.current.info.asInstanceOf[TypeBounds]
          val lambda = bounds.underlying.asInstanceOf[HKTypeLambda]
          val res = lambda.resType

          val current = tree.typeOpt.widenTermRefExpr

          //ctx.log("Witness: " + res.show)
          //ctx.log("Raw:     " + res.toString)
          //ctx.log("Current: " + current.show)
          //ctx.log("Raw:     " + current.toString)
          //ctx.log("subtype?: " + (current.typeConstructor <:< res.typeConstructor).toString)

          if (!(current.typeConstructor <:< res.typeConstructor)) {
            if (tree.symbol.owner.denot == scalaPredefSymbol.denot) {
              report.log("Capturing scala.Predef is currently allowed.", tree.sourcePos)
            }
            else report.error("This spore only allows capturing values wrapped in a " + res.typeConstructor.show+". "
                          +"Consider capturing it as a " + AppliedType(lambda,List(tree.typeOpt.widen)).show + " instead.",tree.sourcePos)
          }
          else report.log("Capturing " + tree.typeOpt.show, tree.sourcePos)
        }
      }
    }
    ctx
  }
}
