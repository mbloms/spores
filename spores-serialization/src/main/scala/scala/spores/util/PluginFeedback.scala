package scala.spores.util

import fansi.Color

object PluginFeedback {
  private def BoldRed(msg: String) = Color.Red(msg).overlay(fansi.Bold.On)
  private def SolutionTemplate(msg: String) =
    s"${Color.Green(fansi.Bold.On.apply("Solution:"))} $msg"

  def unhandledType(tpe: String) =
    BoldRed(s"Type $tpe is not handled.").toString

  def nonSerializableType(owner: String, member: String, tpe: String) = {
    s"Spore contains non-serializable references in $owner: $member with $tpe."
  }

  def stopInspection(owner: String,
                     tparam: String,
                     tpe: Option[String] = None) = {
    s"""${BoldRed(s"Transitive inspection cannot continue beyond $owner:")}
       |  Type parameter ${Color.Red(tparam)} is not fully known at the spore definition site. ${if (tpe.isDefined)
         s"Found ${tpe.get}."}
       |
       |${SolutionTemplate(
         s"Move the spores definition where type $tparam is concrete.")}
     """.stripMargin
  }

  def openClassHierarchy(openClass: String) = {
    s"""${BoldRed(s"Detected open class hierarchy in $openClass.")}
       |  Transitive inspection cannot ensure that ${Color.Red(openClass)} is not being extended somewhere else. For a complete serializable check, class hierarchies need to be closed.
       |
       |${SolutionTemplate(s"Close the class hierarchy by marking super classes as `sealed` and sub classes as `final`.")}
     """.stripMargin
  }

  def nonSerializableTypeParam(owner: String, tparam: String) = {
    s"""${BoldRed(
         s"Type parameter $tparam in $owner does not extend `Serializable` or has an implicit value `CanBeSerialized[$tparam]` in scope.")}
       |
       |${SolutionTemplate(
         s"Define `$tparam` as `$tparam <: Serializable` or extend $tparam with the most precise serializable super class.")}
     """.stripMargin
  }
}
