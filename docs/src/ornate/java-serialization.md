# Spark integration

Spores can be integrated with Spark to provide a safer use of closure.
While they can be used by implicit-based libraries like [Scala Pickling](https://github.com/scala/pickling),
[uPickle](https://github.com/lihaoyi/upickle-pprint),
[Circe](https://github.com/travisbrown/circe) or
[BooPickle](https://github.com/ochrons/boopickle), this tutorial only considers
the integration with `{java.io, scala}.Serializable`-based libraries.

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

Spores allow you to control the environment of a closure. To ensure that spores
are serializable-safe, all the captured variables are checked to be serializable.

This compiler plugin requires you to:
1. Extend `scala.Serializable` in the classes of all the captured types.
1. Close the class hierarchy of all the custom classes that you capture ([what is this?](#closed-class-hierarchies)).

With these requirements, `spores-serialization` will make its best to prove the
correct serializability of your spores.

### Simple example

Primitives, 

### Abstracting over the logic

## Basics

### Closed class hierarchies {.closed-class-hierarchies}

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
and may have a [partial fix soon](https://github.com/scala/scala/pull/5284).

#### The class Metamorphosis

So what should you do to make `spores-serialization` transitively check all your program?
Ensure that all the captured variables in your spores are serializable and closed. If you
have scattered class definitions across different packages, bring them to the same file
and baptise the class hierarchy with `sealed` and `final` as described before.

```scala
import scala.spores._
sealed trait Foo extends Serializable
final class Bar(b: Int) extends Foo
final case class Baz(b: Int) extends Foo

// Force cast to Foo
val foo1: Foo = Baz(1)
val foo2: Foo = new Bar(2)
val bar = new Bar(2)
spore {
  val capturedFoo1 = foo1
  val capturedFoo2 = foo2
  val capturedBar = bar
  (...) => // spore logic
}
```

The previous example compiles. If you don't seal the hierarchy, `spores-serialization` will
output the following error:

```
[warn] /your/path/File.scala:93: Detected open class hierarchy in `trait Foo`.
[warn]   Transitive inspection cannot ensure that trait Foo is not being extended somewhere else. For a complete serializable check, class hierarchies need to be closed.
[warn] 
[warn] Solution: Close the class hierarchy by marking super classes as `sealed` and sub classes as `final`.
[warn]      
[warn]     trait Foo extends Serializable {val foo: String}
[warn]           ^
```

Notice that capturing subclasses like `Bar` or `Baz` will never cause an error
because they are final and, by definition, have no subclass.

#### The escape hatch

The annotation `@assumeClosed` is a escape hatch for users that *for some reason* cannot
turn their class hierarchy closed. It tells the compiler to assume that the class you're
capturing is closed, but unfortunately no analysis of the subclasses will be performed (SI-7046).
Therefore, its use is discouraged and only left for intrepid developers that like risk.

```scala
import scala.spores._
trait Foo extends Serializable
final class Bar(b: Int) extends Foo
final case class Baz(b: Int) extends Foo

// Force cast to Foo
val riskyFoo: Foo = Baz(1)
val s = spore {
  val capturedFoo = (riskyFoo: Foo @assumeClosed)
  (...) => // spore logic
}
```

### Transient fields

In Java, variables may be marked `transient` to indicate that they are not part of the persistent
state of an object and therefore will not be serialized (see [this](https://en.wikibooks.org/wiki/Java_Programming/Keywords/transient) and the [Java Language Specification](http://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.3.1.3)).

In Scala, you can achieve the same goal by annotating the fields with `@transient`.
When `spores-serialization` stumbles upon a transient field, it will ignore its type.

### Making value classes serializable

By definition, value classes can *only* extend `AnyVal`, which means they cannot be `{java.io, scala}.Serializable`.
To overcome this limitation, the compiler plugin uses implicits to prove that a value class `Foo` is serializable.
For proving it, you need to provide an implicit `CanBeSerialized[Foo]` in the scope of
the spore definition.

```scala
import scala.spores._

// Value class definition somewhere
case class Foo(i: Int) extends AnyVal
object Foo {
  implicit object FooIsSerializable extends scala.spores.CanBeSerialized[Foo]
}

// Spore definition somewhere else
import Foo._
val foo = Foo(5)
val s = spore {
  val captured = foo
  (...) => // spore logic
}
```

Thanks to the definition of `FooIsSerializable` and the `import Foo._` in the spore
definition, the compiler plugin is able to prove that `Foo` is indeed serializable.

> {.warning}
> Value classes are not safe to serialize. Its serialization fails when they require
> allocation inside the spore body, because they need to be [instantiated as a class `Foo`
> instead of avoiding the runtime object allocation](http://docs.scala-lang.org/overviews/core/value-classes.html). This happens when:
> 1. a value class is treated as another type.
> 1. a value class is assigned to an array.
> 1. doing runtime type tests, such as pattern matching.
>
> Therefore, you cannot use value classes if you use them in any situation that requires boxing.
> This is a fundamental limitation of value classes that may be fixed in the future. For now,
> the compiler plugin does not catch the misuse of value classes inside spores, so if you decide
> to use them, be careful.


