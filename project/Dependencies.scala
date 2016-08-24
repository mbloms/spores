package scala.spores

import sbt._

object Dependencies {

  val scalaVersion = "2.11.7"

  val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaVersion % "provided"
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % scalaVersion % "test"

  val junit = "junit" % "junit" % "4.12" % "test"
  val junitIntf = "com.novocode" % "junit-interface" % "0.11" % "test"

  val scalaPickling = "org.scala-lang.modules" %% "scala-pickling" % "0.11.0-M2"
  val sourcecode = "com.lihaoyi" %% "sourcecode" % "0.1.2"
  val scalafmt = "com.geirsson" %% "scalafmt" % "0.3.1"
  val twitterUtil = "com.twitter" %% "util-eval" % "6.35.0"
  val docDependencies = Seq(scalafmt, twitterUtil)

  val core = Seq(scalaReflect, scalaCompiler, junit, junitIntf, sourcecode) ++ docDependencies

  val pickling = core ++ Seq(scalaPickling)

}
