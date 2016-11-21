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

## Basics

### Closed class hierarchies

Closed class hierarchies play a key role in Scala because:
* allows the compiler to [assume a closed-world](https://en.wikipedia.org/wiki/Closed-world_assumption) for a concrete set of classes;
* macros and compiler plugins have *full* access to its definition and properties.

This section sheds some light on the differences between the two and why
`spores-serialization` requires closed class hierarchies to ensure the correct
serializability of your program.

#### The class hierarchy

Traditionally, class hierarchies have always be open. Open class hierarchies
are handy and flexible for developers: they allow them to extend classes in any
file or project they want (if they are *visible*). However, such flexibility
hinders the static analysis of programs. For overcoming this limitation, we use
closed class hierarchies.

Open class hierarchy | Closed class hierarchy |
------------ | ---------------|
<code class="language-scala"><span class="hljs-class"><span class="hljs-keyword">trait</span> <span class="hljs-title">Foo</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Serializable</span> | <code class="language-scala"><span class="hljs-keyword">sealed</span> <span class="hljs-class"><span class="hljs-keyword">trait</span> <span class="hljs-title">Foo</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Serializable</span></span></code> | 
<code class="language-scala"> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Bar</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> | <code class="language-scala"><span class="hljs-keyword">final </span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Bar</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> |
<code class="language-scala"><span class="hljs-keyword">case</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Baz</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> | <code class="language-scala"><span class="hljs-keyword">final </span> <span class="hljs-keyword">case</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Baz</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> |

A closed class hierarchy is a finite set of classes that share same hierarchy and are defined
in the same Scala file. They are defined as their counterpart except for the use of extra modifiers.
The rule of thumb is to define all the leafs as `final`, and the root and intermediate traits
and classes as `sealed`. Therefore, whenever you try to extend a sealed trait/class
or a `final` class, the compiler will throw a happy error at you.

#### Motivation of sealed class hierarchies

Transitive checking requires a full traversal of the class hierarchy. It needs to check
 both class and trait definitions and *all their members* to prove that no serialization issues
 can happen at runtime.
 
So, why cannot we just use open class hierarchies?
 
Because:
1. Non-`final` classes can be extended elsewhere. `spores-serialization` could give false
positives if these externally defined classes are not serializable.
1. Intermediate super classes cannot be `final`. Scala needs to guarantee that these classes
are only defined in the current compilation unit, otherwise false positives could happen.
1. The Scala compiler does not give us access to direct subclasses if the class hierarchy
is not sealed. This is a well-known [limitation](https://issues.scala-lang.org/browse/SI-7046) of the Scala typer
and may have a [partial bugfix soon](https://github.com/scala/scala/pull/5284).

#### Abiding by the duty

So what should you do to make `spores-serialization` transitively check all your program?

Ensure that all the captured variables in the spore are serializable.

Consider these two options:
1. Moving all 
