import scala.sys.process._

val majorVersion = "0.27"
val dottyVersion = s"$majorVersion.0-RC1"

ThisBuild / scalaVersion := dottyVersion
ThisBuild / version := "0.1.0"
ThisBuild / organization := "se.mbloms"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-spores",

    libraryDependencies += "ch.epfl.lamp" % s"dotty-compiler_$majorVersion" % dottyVersion,

    //This isn't hacky at all
    test := {
      val x = (Compile / packageBin).value
      val status = Process("make -k", new File("plugin-tests")).!
      if (status != 0)
        throw new TestsFailedException
    },
  )
