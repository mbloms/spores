val majorVersion = "0.27"
val dottyVersion = s"$majorVersion.0-RC1"

ThisBuild / scalaVersion := dottyVersion
ThisBuild / version := "0.1.0"
ThisBuild / organization := "se.mbloms"

//ThisBuild / scalacOptions += "-explain"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-spores",

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test", 
    libraryDependencies += "ch.epfl.lamp" % s"dotty-compiler_$majorVersion" % dottyVersion,
  )
