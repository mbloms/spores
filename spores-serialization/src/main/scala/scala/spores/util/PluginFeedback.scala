package scala.spores.util

object PluginFeedback {
  private def Red(msg: String) =
    Console.RED + msg + Console.RESET
  private def BoldRed(msg: String) =
    Console.BOLD + Console.RED + msg + Console.RESET
  private def SolutionTemplate(msg: String) = {
    Console.GREEN + Console.BOLD + "Solution: " + Console.RESET +
      Console.GREEN + msg + Console.RESET
  }

  def unhandledType(tpe: String) = BoldRed(s"Type $tpe is not handled.")

  def sporesMissing(cls: String) = {
    s"""${BoldRed(
         s"The `spores` macro library is not in the classpath: $cls could not be found.")}
       |
       |${SolutionTemplate(s"""Add to your `build.sbt` file:
         |    libraryDependencies += "ch.epfl.scala" %% "spores" % version""".stripMargin)}
     """.stripMargin
  }

  def nonSerializableType(owner: String, member: String, tpe: String) =
    s"Spore contains non-serializable references in `$owner`: $member of type $tpe."

  def stopInspection(owner: String,
                     tparam: String,
                     tpe: Option[String] = None) = {
    s"""${BoldRed(s"Transitive inspection cannot continue beyond `$owner`:")}
       |  Type parameter ${Red(tparam)} is not fully known at the spore definition site. ${if (tpe.isDefined)
         s"Found ${tpe.get}."}
       |
       |${SolutionTemplate(
         s"Move the spores definition where type $tparam is concrete.")}
     """.stripMargin
  }

  def openClassHierarchy(openClass: String) = {
    s"""${BoldRed(s"Detected open class hierarchy in `$openClass`.")}
       |  Transitive inspection cannot ensure that ${Red(openClass)} is not being extended somewhere else. For a complete serializable check, class hierarchies need to be closed.
       |
       |${SolutionTemplate(
         s"Close the class hierarchy by marking super classes as `sealed` and sub classes as `final`.")}
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
