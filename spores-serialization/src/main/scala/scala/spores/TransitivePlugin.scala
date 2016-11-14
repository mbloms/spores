package scala.spores

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TransitivePlugin(val global: Global) extends Plugin {
  val name = "spores-transitive-plugin"
  val description = "Performs spore transitive checks."
  val components = List[PluginComponent](CheckerComponent)
  val forceTransitiveOption = "force-transitive"
  val forceSerializableTypeParams = "force-serializable-type-parameters"
  val config = PluginConfig(
    super.options.contains(forceTransitiveOption),
    super.options.contains(forceSerializableTypeParams))

  override def init(ops: List[String], e: (String) => Unit): Boolean = true

  // It has to be lazy, otherwise incremental compiler fails :D
  lazy val checker = new TransitiveChecker(TransitivePlugin.this.global)

  private object CheckerComponent extends PluginComponent {
    override val global: checker.global.type = checker.global
    override val phaseName: String = "spores-transitive-checker"
    override val runsAfter: List[String] = List("typer")
    override def newPhase(prev: Phase): Phase = {
      new StdPhase(prev) {
        override def apply(unit: global.CompilationUnit): Unit = {
          new checker.TransitiveTraverser(unit, config).traverse(unit.body)
        }
      }
    }
  }

  override val optionsHelp: Option[String] = Some(s"""
       |-P:spores-transitive-checker:$forceTransitiveOption       Forces failure if the transitive search cannot be completed.
       |-P:spores-transitive-checker:$forceSerializableTypeParams Forces failure if type parameters of classes/traits are not serializable.
    """.stripMargin)
}
