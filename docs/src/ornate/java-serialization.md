# Spark integration

Spores can be integrated with Spark to provide a safer use of closure.
While they can be used by implicit-based libraries like [Scala Pickling](https://github.com/scala/pickling),
[uPickle](https://github.com/lihaoyi/upickle-pprint),
[Circe](https://github.com/travisbrown/circe) or
[BooPickle](https://github.com/ochrons/boopickle), this tutorial only considers
the integration with `{java, scala}.util.Serializable`-based libraries.

## Goal

Help Spark users identify serialization issues at compile-time rather than
runtime. Concretely, this translates into addressing [common](http://www.cakesolutions.net/teamblogs/demystifying-spark-serialisation-error) [1]
[serialization issues](https://databricks.gitbooks.io/databricks-spark-knowledge-base/content/troubleshooting/javaionotserializableexception.html) [2].

## Installation

Add the following sbt settings to your `build.sbt`:
```scala
libraryDependencies += "ch.epfl.scala" % "spores" %% "0.4-M1"
addCompilerPlugin("ch.epfl.scala" % "spores-serialization" %% "0.4-M1")
```

## Quickstart

