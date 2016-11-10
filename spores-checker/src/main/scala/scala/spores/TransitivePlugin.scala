package scala.spores

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TransitivePlugin(val global: Global) extends Plugin {
  val name = "spores-transitive-plugin"
  val description = "Performs spore transitive checks."
  val components = List[PluginComponent](CheckerComponent)

  // It has to be lazy, otherwise incremental compiler fails :D
  lazy val checker = new TransitiveChecker(TransitivePlugin.this.global)

  private object CheckerComponent extends PluginComponent {
    override val global: checker.global.type = checker.global
    override val phaseName: String = "spores-transitive-checker"
    override val runsAfter: List[String] = List("typer")
    override def newPhase(prev: Phase): Phase = {
      new StdPhase(prev) {
        override def apply(unit: global.CompilationUnit): Unit = {
          new checker.TransitiveTraverser(unit).traverse(unit.body)
        }
      }
    }
  }
}
