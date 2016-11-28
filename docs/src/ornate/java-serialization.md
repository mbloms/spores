# Serializing spores

Spores can be serialized using two mechanisms: implicit-based pickling libraries
or `{java.io, scala}.Serializable`-based libraries. In this tutorial, we will
explore the integration of spores with Java-based serialization alternatives,
and show how they can be used in Spark to provide a safer use of closures.

## Goal

Help Spark users identify serialization issues at compile-time rather than
runtime. Concretely, this translates into addressing [common](http://www.cakesolutions.net/teamblogs/demystifying-spark-serialisation-error) [1]
[serialization issues](https://databricks.gitbooks.io/databricks-spark-knowledge-base/content/troubleshooting/javaionotserializableexception.html) [2].

## Installation

Add the following sbt settings to your `build.sbt`:

```scala
resolvers += Resolver.bintrayRepo("scalacenter", "releases")
libraryDependencies += "ch.epfl.scala" %% "spores" % "0.4.0-M5"
addCompilerPlugin("ch.epfl.scala" %% "spores-serialization" % "0.4.0-M5")
```

> {.note}
> These artifacts are only compatible with Scala 2.11.x.
> A [bug in 2.12.0](https://issues.scala-lang.org/browse/SI-10009) prevents us from releasing
> a compatible version. `spores` will be probably released for 2.12.1.

## Quickstart

Spores allow you to control the environment of a closure. To ensure that spores
are serializable-safe, all the captured variables are checked to be serializable.

This compiler plugin requires you to:
1. Extend `scala.Serializable` in the classes of all the captured types.
1. Close the class hierarchy of all the custom classes that you capture ([what is this?](#closed-class-hierarchies)).

With these requirements, `spores-serialization` makes its best to prove the
correct serializability of your spores.

### An example

```scala
import scala.spores._
val s = spore {
  val capturedInt = 8
  val capturedString = "Hello, World!"
  val capturedList = List(1,2,3,4)
  (i: Int) => {
    println(capturedString)
    capturedList.map(_ + i).contains(capturedInt)
  }
}
```

This code snippet compiles because primitives and `{Scala, Java}` collections are serializable.
In it, we capture an `Int`, a `String` and a `List[Int]` which can be successfully
sent over the wire.

But, why? The compiler plugin inspects the type of `capturedInt`, `capturedString` and
`capturedList`. In this case, `Int` is a primitive, `String` extends `java.io.Serializable`
and `List[Int]` is a closed class hierarchy with a primitive (and serializable) type parameter
whose subclasses are safely serializable.

The compiler plugin performs a similar reasoning to the one explained before
to prove that certain types are serializable. Since Scala is very flexible and
allows user to abstract over their logic, `spores-serialization` is able to perform
such analysis in more sophisticated situations.

Before introducing them, let's understand the underlying concepts and guarantees
that the transitive checks provide.

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
  () => // spore logic using `capturedFoo1`, `capturedFoo2` and `capturedBar`
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

#### A escape hatch

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
  () => // spore logic
}
```

### Abstracting over the logic

Sooner or later, your logic may become repetitive. `spores-serialization` is capable
of allowing users to abstract over their logic and define spores at places where
the captured types are not fully defined.

For the following code snippet, assume that `Foo` is a closed class hierarchy.

```scala
import scala.spores._

class Wrapper[T <: Foo](val wrapped: List[T]) {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Int]) => xs.zip(captured)
  }
}
```

And if the wrapper is `Serializable`, you can even send it accross the wire:

```scala
import scala.spores._

class Wrapper[T <: Foo](val wrapped: List[T]) extends Serializable {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Int]) => xs.zip(captured)
  }
}

val wrapper = new Wrapper(List("Hello", "Hello"))
val s = spore {
  val serializedWrapper = wrapper
  () => serializedWrapper
}
```

Why are these working examples? Because `Foo` is ensured to be a high bound of
the type parameter and `Foo` is a closed class hierarchy.

While the previous examples work, users can also set the high bound to be
`Serializable`:

```scala
import scala.spores._

class Wrapper[T <: Serializable](val wrapped: List[T]) {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Int]) => xs.zip(captured)
  }
}
```

But this results in the following warning:

```
TBD
```

Generally, proving that a type parameter extends `scala.Serializable` is not enough
for ensuring the lack of type members because "serializable" classes may have fields
that are not. The previous code snippet is **not the recommended way** to use `spores-serialization`.
It's better to allow the compiler plugin to do all the work if you don't necessarily
like debugging a runtime serialization error a Sunday night.

### Transient fields

In Java, variables may be marked `transient` to indicate that they are not part of the persistent
state of an object and therefore will not be serialized (see [this](https://en.wikibooks.org/wiki/Java_Programming/Keywords/transient) and the [Java Language Specification](http://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.3.1.3)).
In Scala, you can achieve the same goal by annotating the fields with `@transient`.

By definition, transient fields are not part of the analyzed field, and
`spores-serialization` will ignore its type even if it's not serializable.
Under the hood, when Java does initialize the unserialized class, a transient
field will hold no value so make sure that transient fields are not used in the
logic of your program.

### Serializable value classes

By definition, value classes can *only* extend `AnyVal`, which means they cannot be `{java.io, scala}.Serializable`.
To overcome this limitation, the compiler plugin uses implicits to prove that a value class `Foo` is serializable.
For proving it, you need to provide an implicit `CanBeSerialized[Foo]` in the scope of
the spore definition.

```scala
import scala.spores._

// Value class definition somewhere
case class Foo(i: Int) extends AnyVal
object Foo {
  implicit object FooIsSerializable extends CanBeSerialized[Foo]
}

// Spore definition somewhere else
import Foo._
val foo = Foo(5)
val s = spore {
  val captured = foo
  () => // spore logic using `captured`
}
```

Thanks to the definition of `FooIsSerializable` and the `import Foo._` in the spore
definition, the compiler plugin is able to prove that the use of `Foo` is safe.

> {.note}
> Non-primitive value classes *may* be non-serializable. Its serialization fails when they require
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

## Future ideas

As a compiler plugin, `spores-serialization` is capable of doing more than just
static type analysis. Here are some ideas for the future that may be considered
to be implemented depending on the community's response. Come to discuss them
at [Discourse](https://contributors.scala-lang.org/) (**TBD**).

### Warning against the use of well-known 'lazy' methods

Methods whose implementation is lazy are generally not serializable because
they involve the creation of anonymous functions. Some examples are:

1. [`Map.withDefault`](https://issues.scala-lang.org/browse/SI-5018)
1. [`Map.filterKeys`](https://issues.scala-lang.org/browse/SI-6654)
1. [Closures in initializer of structural types break serialization](https://issues.scala-lang.org/browse/SI-5048?jql=status%20=%20Open%20AND%20labels%20=%20serialization)

As these examples are not well-known in the Scala community and still persist,
`spores-serialization` could warn when it detects them inside the spore bodies.

### Kryo support

There may be some missing pieces for working Kryo support. Spark users that
use Kryo instead of Java serialization could get a lot of benefits from using
`spores` and `spores-serialization`.

### Dealing with other serialization issues

Serialization issues may happen when some classes are not in the classloaders,
like [SI-9777](https://issues.scala-lang.org/browse/SI-9777?jql=status%20=%20Open%20AND%20labels%20=%20serialization).
Is there *something* we can do to detect these errors before they happen?
