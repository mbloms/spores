lazy val buildSettings = Seq(
  resolvers += Resolver.bintrayRepo("scalacenter", "releases"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  organization := "ch.epfl.scala",
  organizationName := "Scala Center",
  organizationHomepage := Some(new URL("https://scala.epfl.ch")),
  scalaVersion := "2.11.8",
  // 2.12 is not yet available because of SI-10009
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
  publishArtifact in Test := false,
  bintrayOrganization := Some("scalacenter"),
  bintrayRepository := "releases",
  bintrayPackageLabels :=
    Seq("compiler", "spores", "spark", "serialization", "plugin", "scala"),
  publishTo := (publishTo in bintray).value,
  licenses := Seq(
    "BSD 3-Clause" -> url("http://www.scala-lang.org/downloads/license.html")),
  // Note that original repo is http://github.com/heathermiller/spores
  homepage := Some(url("https://github.com/jvican/spores-spark")),
  startYear := Some(2013),
  autoAPIMappings := true,
  developers := List(
    Developer("heathermiller",
              "Heather Miller",
              "heather.miller@epfl.ch",
              url("http://github.com/heathermiller")),
    Developer("phaller",
              "Philipp Haller",
              "phaller@kth.se",
              url("http://github.com/phaller")),
    Developer("jvican",
              "Jorge Vicente Cantero",
              "jorge.vicentecantero@epfl.ch",
              url("http://github.com/jvican"))
  )
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
  testOptions in Test += Tests.Argument("-v")
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {}
)

lazy val allSettings = commonSettings ++ buildSettings ++ publishSettings

lazy val root = project
  .in(file("."))
  .settings(allSettings)
  .settings(noPublish)
  .aggregate(`spores-core`, `spores-pickling`, `spores-serialization`)
  .dependsOn(`spores-core`)

lazy val `spores-core` = project
  .copy(id = "spores")
  .in(file("core"))
  .settings(allSettings)
  .settings(baseDependencies)
  .settings(
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
  .dependsOn(`spores-core`)
  .settings(
    libraryDependencies +=
      "org.scala-lang.modules" %% "scala-pickling" % "0.11.0-M2",
    parallelExecution in Test := false
    // scalacOptions in Test ++= Seq("-Xlog-implicits")
  )

lazy val `spores-serialization` = project
  .settings(allSettings)
  .settings(baseDependencies)
  .dependsOn(`spores-core`)
  .settings(
    // Make sure that java classes are in the classpath
    compileOrder := CompileOrder.JavaThenScala,
    resourceDirectories in Test +=
      (resourceDirectory in Compile in `spores-core`).value,
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
  .dependsOn(`spores-core`)
  .settings(noPublish)
  .settings(
    dependencyOverrides += "com.lihaoyi" %% "scalaparse" % "0.3.1"
  )
