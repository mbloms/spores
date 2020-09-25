import scala.sys.process._

val majorVersion = "0.27"
val dottyVersion = s"$majorVersion.0-RC1"

ThisBuild / scalaVersion := dottyVersion
ThisBuild / version := "0.1.0"
ThisBuild / organization := "se.mbloms"

lazy val dottyMk = taskKey[Unit]("put dottyVersion in plugin-tests/dotty.mk")

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-spores",

    libraryDependencies += "ch.epfl.lamp" % s"dotty-compiler_$majorVersion" % dottyVersion,

    dottyMk := {
      val (art, jar) = (Compile / packageBin / packagedArtifact).value
      IO.write(new File("plugin-tests/dotty.mk"),s"""
dottyVersion := ${dottyVersion}
JAR := ${jar.getAbsolutePath}
""")
    },
    //This isn't hacky at all
    test := {
      dottyMk.value
      (Compile / packageBin).value
      val status = Process("make -k", new File("plugin-tests")).!
      if (status != 0)
        throw new TestsFailedException
    },
  )
