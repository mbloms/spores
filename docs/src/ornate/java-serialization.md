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
libraryDependencies += "ch.epfl.scala" %% "spores" % "0.4.3"
addCompilerPlugin("ch.epfl.scala" %% "spores-serialization" % "0.4.3")
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

With these requirements, `spores-serialization` makes a best effort to ensure the
correct serializability of your spores.

### An example

```tut:book
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
and `List[Int]` is a closed class hierarchy with a primitive (and serializable) type argument
whose subclasses are safely serializable.

The compiler plugin performs a similar reasoning to the one explained before
to verify that certain types are serializable. Since Scala is very flexible and
allows users to abstract over their logic, `spores-serialization` is able to perform
such analysis in more sophisticated situations.

Before introducing them, let's understand the underlying concepts and guarantees
that the transitive checks provide.

## Basics

### Closed class hierarchies {.closed-class-hierarchies}

Closed class hierarchies play a key role in Scala because:
* they allow the compiler to [assume a closed-world](https://en.wikipedia.org/wiki/Closed-world_assumption) for a concrete set of classes;
* macros and compiler plugins have *full* access to their definition and properties.

This section sheds some light on the differences between the two and why
`spores-serialization` requires closed class hierarchies to ensure the correct
serializability of your program.

#### The class hierarchy

Traditionally, class hierarchies have always be open. Open class hierarchies
are handy and flexible for developers: they allow them to extend classes in any
file or project they want (if they are *visible*). However, such flexibility
hinders the static analysis of programs. To overcome this limitation, we use
closed class hierarchies.

Open class hierarchy | Closed class hierarchy |
------------ | ---------------|
<code class="language-scala"><span class="hljs-class"><span class="hljs-keyword">trait</span> <span class="hljs-title">Foo</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Serializable</span> | <code class="language-scala"><span class="hljs-keyword">sealed</span> <span class="hljs-class"><span class="hljs-keyword">trait</span> <span class="hljs-title">Foo</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Serializable</span></span></code> | 
<code class="language-scala"> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Bar</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> | <code class="language-scala"><span class="hljs-keyword">final </span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Bar</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> |
<code class="language-scala"><span class="hljs-keyword">case</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Baz</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> | <code class="language-scala"><span class="hljs-keyword">final </span> <span class="hljs-keyword">case</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Baz</span>(<span class="hljs-params">b: Int</span>) <span class="hljs-keyword">extends</span> <span class="hljs-title">Foo</span></span></code> |

A closed class hierarchy is a finite set of classes that share the same hierarchy and are defined
in the same Scala file. They are defined as their counterpart except for the use of extra modifiers.
The rule of thumb is to define all the leafs as `final`, and the root and intermediate traits
and classes as `sealed`. Therefore, whenever you try to extend a sealed trait/class
or a `final` class, the compiler will throw a happy error at you.

#### Motivation of sealed class hierarchies

Transitive checking requires a full traversal of the class hierarchy. It needs to check
 both class and trait definitions and *all their members* to prove that no serialization issues
 can happen at runtime.
 
So, why can't we just use open class hierarchies?
 
Because:
1. Non-`final` classes can be extended elsewhere. `spores-serialization` could give false
positives if these externally defined classes are not serializable.
1. Intermediate super classes cannot be `final`. Scala needs to guarantee that these classes
are only defined in the current compilation unit, otherwise false positives could happen.
1. The Scala compiler does not give us access to direct subclasses if the class hierarchy is not sealed.

#### The class Metamorphosis

So what should you do to make `spores-serialization` transitively check all your program?
Ensure that all the captured variables in your spores are serializable and closed. If you
have class definitions scattered across different packages, bring them to the same file
and baptise the class hierarchy with `sealed` and `final` as described before.

```tut:book
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

The previous example compiles as expected. Let's see what happens with an open class hierarchy:

```tut:book
trait Foo extends Serializable
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

Note that capturing subclasses like `Bar` or `Baz` is fine because they are
final and, by definition, have no subclasses.

#### An escape hatch

The annotation `@assumeClosed` is an escape hatch for users that *for some reason* cannot
close their class hierarchy. The annotation tells the compiler to assume that the class you're
capturing is closed, but unfortunately no analysis of the subclasses is performed (SI-7046).
Therefore, its use is discouraged and only left for intrepid developers that like risk.

```tut:book
import scala.spores._
trait Foo extends Serializable
final class Bar(b: Int) extends Foo
final case class Baz(s: String) extends Foo

// Force cast to Foo
val riskyFoo: Foo = Baz("1")
val s = spore {
  val capturedFoo = (riskyFoo: Foo @assumeClosed)
  () => // spore logic
}
```

### Abstracting over the logic

Sooner or later, your logic may become repetitive. `spores-serialization` is capable
of allowing users to abstract over their logic and define spores in places where
the captured types are not fully defined.

For the following code snippet, assume that `Foo` is a closed class hierarchy.

```tut:book
import scala.spores._

// Foo is now a closed class hierarchy
sealed trait Foo extends Serializable
final class Bar(b: Int) extends Foo
final case class Baz(s: String) extends Foo

class Wrapper[T <: Foo](val wrapped: List[T]) {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Foo]) => xs.zip(captured)
  }
}
```

And if the wrapper is `Serializable`, you can even send it accross the wire:

```tut:book
import scala.spores._

sealed class Wrapper[T <: Foo](val wrapped: List[T]) extends Serializable {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Foo]) => xs.zip(captured)
  }
}

val wrapper = new Wrapper(List(Baz("Hello"), Baz("Hello")))
val s = spore {
  val serializedWrapper = wrapper
  () => serializedWrapper
}
```

Note that these examples work because `Foo` is ensured to be an upper bound of
the type parameter and `Foo` is a closed class hierarchy.

Users can also set the upper bound to be `Serializable`:

```tut:book
import scala.spores._

class SerializableWrapper[T <: Serializable](val wrapped: List[T]) {
  val zippingSpore = spore {
    val captured = wrapped
    (xs: List[Int]) => xs.zip(captured)
  }
}
```

But this is totally discouraged as it defeats the purpose of the transitive checker,
which cannot prove from this point the full serializability of every type parameter of
`SerializableWrapper`.

Verifying that a type parameter extends `scala.Serializable` is not enough
to ensure the lack of type members because "serializable" classes may have fields
that are not.

### Transient fields

In Java, variables may be marked `transient` to indicate that they are not part of the persistent
state of an object and are therefore not serialized (see [this](https://en.wikibooks.org/wiki/Java_Programming/Keywords/transient) and the [Java Language Specification](http://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.3.1.3)).
In Scala, you can achieve the same goal by annotating the fields with `@transient`.

By definition, transient fields are not part of the analyzed field, and
`spores-serialization` ignores its type even if it's not serializable.
Under the hood, when Java initializes the deserialized class instance, a transient
field will hold no value, so make sure that transient fields are not used in the
logic of your program.

### Serializable value classes

By definition, value classes can *only* extend `AnyVal`, which means they cannot be `{java.io, scala}.Serializable`.
To overcome this limitation, the compiler plugin uses implicits to prove that a value class `FooVal` is serializable.
For proving it, you need to provide an implicit `CanSerialize[FooVal]` in the scope of
the spore definition.

```tut:book
import scala.spores._

// Value class definition somewhere
case class FooVal(i: Int) extends AnyVal
object FooImplicit {
  implicit object FooIsSerializable extends CanSerialize[FooVal]
}

// Spore definition somewhere else
val foo = FooVal(5)
import FooImplicit._
val s = spore {
  val captured = foo
  () => // spore logic using `captured`
}
```

Thanks to the definition of `FooIsSerializable` and the `import FooImplicit._` in the spore
definition, the compiler plugin is able to prove that the use of `FooVal` is safe.

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

Do you want `spores-serialization` to be smarter? To support a use case that it's
not yet implemented?

As a compiler plugin, `spores-serialization` is capable of doing more than just
static type analysis. [Here are some ideas for the future](https://github.com/jvican/spores/issues)
that may be considered to be implemented depending on the community's response. Come to discuss them
at [Discourse](https://contributors.scala-lang.org/)(**TBD**) or the issue tracker,
and propose your ideas to make everyone's life easier.


