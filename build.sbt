import scala.spores.Dependencies

lazy val buildSettings = Seq(
  organization := "org.scala-lang.modules",
  organizationName := "LAMP/EPFL",
  organizationHomepage := Some(new URL("http://lamp.epfl.ch")),
  version := "0.3.0-SNAPSHOT",
  scalaVersion := "2.11.7"
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { x =>
    false
  },
  publishArtifact in Test := false,
  licenses := Seq(
    "BSD 3-Clause License" -> url(
      "http://www.scala-lang.org/downloads/license.html")),
  homepage := Some(url("https://github.com/heathermiller/spores")),
  startYear := Some(2013),
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/heathermiller/spores"),
      "scm:git:git@github.com:heathermiller/spores.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>heathermiller</id>
        <name>Heather Miller</name>
        <timezone>+1</timezone>
        <url>http://github.com/heathermiller</url>
      </developer>
      <developer>
        <id>phaller</id>
        <name>Philipp Haller</name>
        <timezone>+1</timezone>
        <url>http://github.com/phaller</url>
      </developer>
      <developer>
        <id>jvican</id>
        <name>Jorge Vicente Cantero</name>
        <timezone>+1</timezone>
        <url>https://github.com/jvican</url>
      </developer>
    </developers>
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-target:jvm-1.6",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Xlog-reflective-calls",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)

lazy val commonSettings = Seq(
  triggeredMessage in ThisBuild := Watched.clearWhenTriggered,
  watchSources += baseDirectory.value / "resources",
  testOptions in Test += Tests
    .Argument(TestFrameworks.JUnit, "-q", "-v", "-s"),
  scalacOptions in (Compile, console) ++= compilerOptions,
  resolvers += Resolver.sonatypeRepo("snapshots")
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {}
)

lazy val allSettings = commonSettings ++ buildSettings ++ publishSettings

lazy val root = project
  .copy(id = "spores")
  .in(file("."))
  .settings(allSettings)
  .settings(noPublish)
  .aggregate(core, pickling)
  .dependsOn(core)

lazy val core = project
  .copy(id = "spores-core")
  .in(file("core"))
  .settings(allSettings)
  .settings(
    moduleName := "spores-core",
    resourceDirectory in Compile := baseDirectory.value / "resources",
    test in Test <<=
      (test in Test) dependsOn (toolboxClasspath in Test),
    libraryDependencies ++= Dependencies.core,
    parallelExecution in Test := false
  )

/* Write all the compile-time dependencies of the spores macro to a file,
 * in order to read it from the created Toolbox to run the neg tests. */
lazy val toolboxClasspath = taskKey[Unit]("Write Toolbox's classpath.")
toolboxClasspath in Test in core := {
  val classpathAttributes = (dependencyClasspath in Compile in core).value
  val dependenciesClasspath =
    classpathAttributes.map(_.data.getAbsolutePath).mkString(":")
  val scalaBinVersion = (scalaBinaryVersion in Compile in core).value
  val targetDir = (target in Compile in core).value.getAbsolutePath
  val compiledClassesDir = s"$targetDir/scala-$scalaBinVersion/classes"
  val classpath = s"$compiledClassesDir:$dependenciesClasspath"
  val resourceDir = (resourceDirectory in Compile in core).value
  resourceDir.mkdir() // In case it doesn't exist
  val resourcePath = resourceDir.getAbsolutePath
  val classpathPath = s"$resourcePath/toolbox.classpath"
  scala.tools.nsc.io.File(classpathPath).writeAll(classpath)
}

lazy val sporesSpark = project
  .copy(id = "spores-spark")
  .in(file("spores-spark"))
  .settings(allSettings)
  .settings(noPublish)
  .dependsOn(core)
  .settings(
    libraryDependencies <<= libraryDependencies in core,
    (test in Test) <<=
      (test in Test) dependsOn (crossTest in Test),
    (unsetSparkEnv in Test) <<=
      (unsetSparkEnv in Test) triggeredBy (test in Test),
    (clean in Compile) <<=
      (clean in Compile) dependsOn (clean in Compile in core)
  )

val sparkEnv = "spores.spark"

/* Run the test suite of core and then set the spark env and run tests. */
// TODO(jvican): Add support for testOnly
lazy val crossTest = taskKey[Unit]("Enable spark at compilation-time.")
crossTest in Test in sporesSpark := {
  // Check spark env is disabled before running normal tests
  if (System.getProperty(sparkEnv, "false").toBoolean)
    System.setProperty(sparkEnv, "false")
  // Run normal tests
  (test in Test in core).value
  // Set spark environment before running spark tests
  System.setProperty(sparkEnv, "true")
}

lazy val unsetSparkEnv = taskKey[Unit]("Disable spark environment")
unsetSparkEnv in Test in sporesSpark := {
  System.setProperty(sparkEnv, "false")
}

lazy val pickling = project
  .copy(id = "spores-pickling")
  .in(file("spores-pickling"))
  .settings(allSettings)
  .settings(
    libraryDependencies ++= Dependencies.pickling,
    parallelExecution in Test := false
    // scalacOptions in Test ++= Seq("-Xlog-implicits")
  )
  .dependsOn(core)

lazy val readme = scalatex
  .ScalatexReadme(
    projectId = "readme",
    wd = file(""),
    url = "https://github.com/jvican/spores/tree/master",
    source = "Readme"
  )
  .dependsOn(core)
  .settings(noPublish)
  .settings(
    dependencyOverrides += "com.lihaoyi" %% "scalaparse" % "0.3.1"
  )
