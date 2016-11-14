package scala.spores.util

import fansi.Color

object PluginFeedback {
  def NonSerializableType(owner: String, member: String, tpe: String) =
    s"Spore contains non-serializable references in $owner: $member with $tpe."

  def StoppedTransitiveInspection(owner0: String, tparam0: String) = {
    val owner = Color.Red(owner0)
    val tparam = Color.Red(tparam0)
    s"""${Color.Red(s"Transitive inspection cannot continue beyond $owner:")}
       |  Type parameter $tparam is not fully known at the spore definition site.
       |
       |${Color.Green(s"Solution: Move the spores definition wherever type $tparam0 is concrete.")}
     """.stripMargin
  }

}
