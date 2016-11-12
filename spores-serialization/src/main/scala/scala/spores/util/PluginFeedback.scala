package scala.spores.util

object PluginFeedback {
  def NonSerializableType(owner: String, member: String, tpe: String) =
    s"Spore contains non-serializable references in $owner: $member with $tpe."
}
