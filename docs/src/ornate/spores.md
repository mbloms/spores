# Spores
[![Build Status](https://platform-ci.scala-lang.org/api/badges/scalacenter/spores/status.svg)](https://platform-ci.scala-lang.org/scalacenter/spores)
[![Gitter](https://badges.gitter.im/scalacenter/spores.svg)](https://gitter.im/scalacenter/spores?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Maven Central](https://img.shields.io/maven-central/v/ch.epfl.scala/spores_2.11.svg)][search.maven]

Scala Spores, safe mobile closures: [SIP-21](http://docs.scala-lang.org/sips/pending/spores.html).

Spores is an extension of the Scala compiler that enables a safer use of closures
in concurrent and distributed environments. It allows developers to guarantee properties
of functions based on types, having more control over the function's environment.

In order to provide this, spores augment the `Function` type with type information
of the captured variables. Captured variables are terms defined outside of the function
definition and used throughout the closure body. `Spores` are designed to be composable
in both logic and properties.

> {.note}
> Please, help us improve the project by [filing any issue you may encounter](https://github.com/jvican/spores/issues/new)!

## Introduction

Functional programming languages are regularly touted as an enabling force, as
an increasing number of applications become concurrent and distributed.
However, managing closures in a concurrent or distributed environment, or
writing APIs to be used by clients in such an environment, remains
considerably precarious-- complicated environments can be captured by these
closures, which regularly leads to a whole host of potential hazards across
libraries/frameworks in Scala's standard library and its ecosystem.

Potential hazards when using closures incorrectly:

- Memory leaks
- Race conditions, due to capturing mutable references
- Runtime serialization errors, due to unintended capture of references

This SIP outlines an abstraction, called _spores_, which enables safer use of
closures in concurrent and distributed environments. This is achieved by
controlling the environment which a spore can capture. Using an
_assignment-on-capture_ semantics, certain concurrency bugs due to capturing mutable
references can be avoided.

## Motivation

### Serialization

The following example uses Java Serialization to serialize a closure. However,
serialization fails with a `NotSerializableException` due to the unintended
capture of a reference to an enclosing object.

```scala
case class Helper(name: String)

class Main {
  val helper = Helper("the helper")

  val fun: Int => Unit = (x: Int) => {
    val result = x + " " + helper.toString
    println("The result is: " + result)
  }
}
```

Given the above class definitions, serializing the `fun` member of an instance
of `Main` throws a `NotSerializableException`. This is unexpected, since `fun`
refers only to serializable objects: `x` (an `Int`) and `helper` (an instance
of a case class).

Here is an explanation of why the serialization of `fun` fails: since `helper`
is a field, it is not actually copied when it is captured by the closure.
Instead, when accessing helper its getter is invoked. This can be made
explicit by replacing `helper.toString` by the invocation of its getter,
`this.helper.toString`. Consequently, the `fun` closure captures `this`, not
just a copy of `helper`. However, `this` is a reference to class `Main` which
is not serializable.

The above example is not the only possible situation in which a closure can
capture a reference to `this` or to an enclosing object in an unintended way.
Thus, runtime errors when serializing closures are common.

## Getting started

Start reading the [Getting Started section](getting-started.md).

## About

The Scala Center is supporting this project under the [Advisory Board proposal submitted
by IBM](https://github.com/scalacenter/advisoryboard/blob/master/proposals/006-compile-time-serializibility-check.md).
If you want to read more about the underlying theory, check the paper [Spores: A Type-based Foundation
for Closures in the Age of Concurrency and Distribution](https://infoscience.epfl.ch/record/191239/files/spores_1.pdf)
by [Heather Miller](https://heather.miller.am) and [Philipp Haller](http://lampwww.epfl.ch/~phaller/).

[search.maven]: http://search.maven.org/#search|ga|1|ch.epfl.scala.spores
