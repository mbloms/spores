package scala.spores.util

trait CheckerUtils[G <: scala.tools.nsc.Global] {
  val global: G
  import global._

  /* This beauty here cannot have any dependency on external libraries b/c
   * it's executed when these libraries (and spores) are not in the classpath. */
  @inline def lifeVest[T](thunk: => T) = {
    import scala.spores.util.NoDependencyPluginFeedback.missingClass
    try { thunk } catch {
      case n: NoClassDefFoundError => abort(missingClass(n.getMessage))
    }
  }

  def debug[T](es: sourcecode.Text[T]*)(implicit line: sourcecode.Line,
                                        file: sourcecode.File): Unit = {
    es.foreach { e =>
      val filename = file.value.replaceAll(".*/", "")
      val header = Console.GREEN + s"$filename:${line.value}"
      debuglog(s"$header ${Console.RESET}${e.value}")
    }
  }
}
