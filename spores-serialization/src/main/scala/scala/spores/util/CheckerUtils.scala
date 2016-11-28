package scala.spores.util

trait CheckerUtils[G <: scala.tools.nsc.Global] {
  val global: G
  import global._

  /* This beauty here cannot is executed when spores is not in the classpath */
  @inline def lifeVest[T](thunk: => T) = {
    try { thunk } catch {
      case n: NoClassDefFoundError =>
        abort(PluginFeedback.sporesMissing(n.getMessage))
    }
  }
}
