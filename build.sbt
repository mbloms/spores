lazy val buildSettings = Seq(
  organization := "org.scala-lang.modules",
  organizationName := "LAMP/EPFL",
  organizationHomepage := Some(new URL("http://lamp.epfl.ch")),
  version := "0.3.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.0"),
  fork in Test := true
)

lazy val testDependencies = Seq(
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

lazy val baseDependencies = {
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test",
    "com.lihaoyi" %% "sourcecode" % "0.1.3"
  ) ++ testDependencies
}

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
  .aggregate(core, `spores-pickling`, `spores-checker`)
  .dependsOn(core)

lazy val core = project
  .copy(id = "spores-core")
  .in(file("core"))
  .settings(allSettings)
  .settings(baseDependencies)
  .settings(
    moduleName := "spores-core",
    resourceDirectory in Compile := baseDirectory.value / "resources",
    parallelExecution in Test := false,
    /* Write all the compile-time dependencies of the spores macro to a file,
     * in order to read it from the created Toolbox to run the neg tests. */
    resourceGenerators in Compile += Def.task {
      val classpathAttributes = (dependencyClasspath in Compile).value
      val dependenciesClasspath =
        classpathAttributes.map(_.data.getAbsolutePath).mkString(":")
      val scalaBinVersion = (scalaBinaryVersion in Compile).value
      val targetDir = (target in Compile).value
      val compiledClassesDir = targetDir / s"scala-$scalaBinVersion/classes"
      val classpath = s"$compiledClassesDir:$dependenciesClasspath"
      val resourceDir = (resourceDirectory in Compile).value
      resourceDir.mkdir() // In case it doesn't exist
      val toolboxTestClasspath = resourceDir / "toolbox.classpath"
      IO.write(toolboxTestClasspath, classpath)
      List(toolboxTestClasspath.getAbsoluteFile)
    }.taskValue
  )

lazy val `spores-pickling` = project
  .settings(allSettings)
  .settings(baseDependencies)
  .dependsOn(core)
  .settings(
    libraryDependencies +=
      "org.scala-lang.modules" %% "scala-pickling" % "0.11.0-M2",
    parallelExecution in Test := false
    // scalacOptions in Test ++= Seq("-Xlog-implicits")
  )

lazy val `spores-checker` = project
  .settings(allSettings)
  .settings(baseDependencies)
  .dependsOn(core)
  .settings(
    // Make sure that java classes are in the classpath
    compileOrder := CompileOrder.JavaThenScala,
    resourceDirectories in Test +=
      (resourceDirectory in Compile in core).value,
    libraryDependencies +=
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    scalacOptions in Test ++= {
      val compiledPlugin = (packageBin in Compile).value
      Seq(
        s"-Xplugin:${compiledPlugin.getAbsolutePath}",
        s"-Jdummy=${compiledPlugin.lastModified}"
      )
    },
    resourceGenerators in Test += Def.task {
      val extraOptions = (scalacOptions in Test).value.mkString(" ")
      val resourceDir = (resourceDirectory in Test).value
      val extraOptionsFile = resourceDir / "toolbox.extra"
      IO.write(extraOptionsFile, extraOptions)
      List(extraOptionsFile.getAbsoluteFile)
    }.taskValue
  )

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
