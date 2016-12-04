# Getting started

## Installation

Add the following sbt settings to your `build.sbt`:

```scala
libraryDependencies += "ch.epfl.scala" %% "spores" % "0.4.3"
```

> {.note}
> This artifacts is only compatible with Scala 2.11.x.
> A [bug in 2.12.0](https://issues.scala-lang.org/browse/SI-10009) prevents us from releasing
> a compatible version. `spores` will be probably released for 2.12.1.

> {.warning}
> If you want to use spores for serialization, read the [Basics](#basics)
> and jump to the [Java Serialization guide](java-serialization.md).

## Basics {.basics}

To use spores, first import them:

```tut
import scala.spores._
```

Spores have a few modes of usage. The simplest form is:

```tut
val outerInt = 1717
val s = spore {
  val i = outerInt
  (x: Int) => {
    val result = x + " " + i.toString
    println("The result is: " + result)
  }
}
```

In this example, no transformation is actually performed. Instead, the
compiler simply ensures that the spore is _well-formed_, i.e., anything that's
captured is explicitly listed as a value definition before the spore's
closure. This ensures that the enclosing `this` instance is not accidentally
captured, in this example.

Spores can also be used in for-comprehensions:

```scala
for { i <- collection
      j <- doSomething(i)
} yield s"${capture(i)}: result: $j"
```

Here, the fact that a spore is created is implicit, that is, the `spore`
marker is not used explicitly. Spores come into play because the underlying
`map` method of the type of `doSomething(i)` takes a spore as a parameter. The
`capture(i)` syntax is an alternative way of declaring captured variables, in
particular for use in for-comprehensions.

Finally, a regular function literal can be used as a spore. That is, a method
that expects a spore can be passed a function literal so long as the function
literal is well-formed.

```tut
def sendOverWire(s: Spore[Int, Int]): Unit = println(s)
sendOverWire((x: Int) => x * x - 2)
```

## Design

The main idea behind spores is to provide an alternative way to create
closure-like objects, in a way where the environment is controlled.

A spore is created as follows.

```tut
val outerInt = 1717
val s = spore {
  val i = outerInt
  (x: Int) => {
    val result = x + " " + i.toString
    println("The result is: " + result)
  }
}
```

The body of a spore consists of two parts:

1. **the spore header:** a sequence of local value (val) declarations only, and
2. **the closure**.

In general, a `spore { ... }` expression has the following shape.

Note that the value declarations described in point 1 above can be `implicit`
but not `lazy`.

```scala
spore {
  val x_1: T_1 = init_1
  ...
  val x_n: T_n = init_n
  (p_1: S_1, ..., p_m: S_m) => {
    <body>
  }
}
```

The types `T_1, ..., T_n` can also be inferred.

The closure of a spore has to satisfy the following rule. All free variables
of the closure body have to be either:

1. parameters of the closure, or
2. declared in the preceding sequence of local value declarations,
3. marked using `capture` (see corresponding section below), or
4. *statically* accessible (members of objects | packages).

```tut
case class Person(name: String, age: Int)
val outer1 = 0
val outer2 = Person("Jim", 35)
val s = spore {
  val inner = outer2
  (x: Int) => {
    s"The result is: ${x + inner.age + outer1}"
  }
}
```

In the above example, the spore's closure is invalid, and would be rejected
during compilation. The reason is that the variable `outer1` is neither a
parameter of the closure nor one of the spore's value declarations (the only
value declaration is: `val inner = outer2`).

### Evaluation Semantics
<a name="evaluation-semantics"></a>

In order to make the runtime behavior of a spore as intuitive as possible, the
design leaves the evaluation semantics unchanged compared to regular closures.
Basically, leaving out the `spore` marker results in a closure with the same
runtime behavior.

For example,

```scala
spore {
  val l = this.logger
  () => new LoggingActor(l)
}
```

and

```scala
{
  val l = this.logger
  () => new LoggingActor(l)
}
```

have the same behavior at runtime. The rationale for this design decision is
that the runtime behavior of closure-heavy code can already be hard to reason
about. It would become even more difficult if we would introduce additional
rules for spores.

### Spore Type

The type of the spore is determined by the type and arity of the closure. If
the closure has type `A => B`, then the spore has type `Spore[A, B]`. For
convenience we also define spore types for two or more parameters.

In example 3, the type of s is `Spore[Int, Unit]`.
Implementation
The spore construct is a macro which

- performs the checking described above, and which
- replaces the spore body so that it creates an instance of one of the Spore traits, according to the arity of the closure of the spore.

The `Spore` trait for spores of arity 1 is declared as follows:

```scala
trait Spore[-T, +R] extends Function1[T, R]
```

For each function arity there exists a corresponding `Spore` trait of the same
arity (called `Spore2`, `Spore3`, etc.)

### Implicit Conversion

Regular function literals can be implicitly converted to spores. This implicit
conversion has two benefits:

1. it enables the use of spores in for-comprehensions.
2. it makes the spore syntax more lightweight, which is important in frameworks such as [Spark](http://spark.incubator.apache.org/) where users often create many small function literals.

This conversion is defined as a member of the `Spore` companion object, so
it's always in the implicit scope when passing a function literal as a method
argument when a `Spore` is expected. For example, one can do the following:

```scala
def sendOverWire(s: Spore[Int, Int]): Unit = println(s)
sendOverWire((x: Int) => x * x - 2)
```

This is arguably much lighter-weight than having to declare a spore before
passing it to `sendOverWire`.

In general, the implicit conversion will be successful if and only if the
function literal is well-formed according to the spore rules (defined above in
the _Design_ section). Note that _only function literals can be converted to spores_.
This is due to the fact that the body of the function literal has to be checked
by the spore macro to make sure that the conversion is safe. For _named_ function
values (i.e., not literals) on the other hand, it's not guaranteed that the
function value's body is available for the spore macro to check.

### Capture Syntax and For-Comprehensions

To enable the use of spores with for-comprehensions, a `capture` syntax has
been introduced to assist in the spore checking.

To see why this is necessary, let's start with an example. Suppose we have a
type for distributed collections:

```scala
trait DCollection[A] {
  def map[B](sp: Spore[A, B]): DCollection[B]
  def flatMap[B](sp: Spore[A, DCollection[B]]): DCollection[B]
}
```

This type, `DCollection`, might be implemented in a way where the data is
distributed across machines in a cluster. Thus, the functions passed to `map`,
`flatMap`, etc. have to be serializable. A simple way to ensure this is to
require these arguments to be spores. However, we also would like for-comprehensions
like the following to work:

```scala
def lookup(i: Int): DCollection[Int] = ...
val indices: DCollection[Int] = ...

for { i <- indices
      j <- lookup(i)
} yield j + i
```

A problem here is that the desugaring done by the compiler for
for-comprehensions doesn't know anything about spores. This is what
the compiler produces from the above expression:

```scala
indices.flatMap(i => lookup(i).map(j => j + i))
```

The problem is that `(j => j + i)` is not a spore. Furthermore, making it a
spore is not straightforward, as we can't change the way for-comprehensions
are translated.

We can overcome this by using the implicit conversion introduced in the
previous section to convert the function literal implicitly to a spore.

However, in continuing to look at this example, it's evident that the lambda
still has the wrong shape. The captured variable `i` is not declared in the
spore header (the list of value definitions preceding the closure within the
spore), like a spore demands.

We can overcome this using the `capture` syntax. That is, instead of having to write:

```scala
{
  val captured = i
  j => j + i
}
```

One can also write:

```scala
(j => j + capture(i))
```

Thus, the above for-comprehension can be rewritten using spores and `capture`
as follows:

```scala
for { i <- indices
      j <- lookup(i)
} yield j + capture(i)
```

Here, `i` is "captured" as it occurs syntactically after the arrow of another
generator (it occurs after `j <- lookup(i)`, the second generator in the
for-comprehension).

> {.note}
> `capture` can only capture identifiers. This means that paths like
> `capture(foo.bar.baz)` will fail and should be rewritten to `capture(foo).bar.baz`.
>  
> The reason why captured expressions are restricted to identifiers is that
> otherwise the `capture` function will change the evaluation semantics.
> Removing `spore` from the block could potentially change the way the captured
> expressions are evaluated. This would complicate the reasoning about spore-based
> code (see the section [Evaluation Semantics](#evaluation-semantics) above).

### Macro Expansion

An invocation of the spore macro expands the spore's body as follows. Given
the general shape of a spore as shown above, the spore macro produces the
following code:

```scala
new <spore implementation class>[S_1, ..., S_m, R]({
  val x_1: T_1 = init_1
  ...
  val x_n: T_n = init_n
  (p_1: S_1, ..., p_m: S_m) => {
    <body>
  }
})
```

Note that, after checking, the spore macro need not do any further
transformation, since implementation details such as unneeded remaining outer
references are removed by the new backend intended for inclusion in Scala
2.11. It's also useful to note that in some cases these unwanted outer
references are already removed by the existing backend.

The spore implementation classes follow a simple pattern. For example, for
arity 1, the implementation class is declared as follows:

```scala
class SporeImpl[-T, +R](f: T => R) extends Spore[T, R] {
  def apply(x: T): R = f(x)
}
```

### Type Inference

Similar to regular functions and closures, the type of a spore should be
inferred. Inferring the type of a spore amounts to inferring the type
arguments when instantiating a spore implementation class:

```scala
new <spore implementation class>[S_1, ..., S_m, R]({
  // ...
})
```

In the above expression, the type arguments `S_1, ..., S_m`, and `R` should be
inferred from the expected type.

Our current proposal is to solve this type inference problem in the context of
the integration of Java SAM closures into Scala. Given that it is planned to
eventually support such closures, and to support type inference for these
closures as well, we plan to piggyback on the work done on type inference for
SAMs in general to achieve type inference for spores.
