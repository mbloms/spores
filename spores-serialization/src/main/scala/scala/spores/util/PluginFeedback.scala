package scala.spores.util

object PluginFeedback {
  def NonSerializableType(owner: String, member: String, tpe: String) =
    s"Spore contains non-serializable references in $owner: $member with $tpe."

  def StoppedTransitiveInspection(t: String, tparam: String) =
    s"Transitive inspection cannot continue: $t has a type parameter $tparam whose type is not fully known at the spore definition site."
}
