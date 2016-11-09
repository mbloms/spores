package scala.spores

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TransitiveSporesPlugin(val global: Global) extends Plugin {
  import global._

  val name = "spores-transitive-plugin"
  val description = "Performs spore transitive checks."
  val components = List[PluginComponent](CheckerComponent)

  private object CheckerComponent extends PluginComponent {
    val global = TransitiveSporesPlugin.this.global
    import global._

    override val phaseName: String = "spores-transitive-checker"
    override val runsAfter: List[String] = List("typer")

    class TransitiveTraverser(unit: CompilationUnit) extends Traverser {
      override def traverse(tree: Tree): Unit = tree match {
        case cls: ClassDef =>
          val accessors = cls.symbol.info.members.filter(_.isAccessor).toList
          reporter.info(cls.pos,
                        s"Found in ${cls.name.decodedName}: $accessors",
                        force = true)
        case _ => super.traverse(tree)
      }
    }

    override def newPhase(prev: Phase): Phase = {
      new StdPhase(prev) {
        override def apply(unit: CompilationUnit) = {
          new TransitiveTraverser(unit).traverse(unit.body)
        }
      }
    }
  }

}
