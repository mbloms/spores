# Spores
[![Build Status](https://platform-ci.scala-lang.org/api/badges/jvican/spores-spark/status.svg)](https://platform-ci.scala-lang.org/jvican/spores-spark)

Scala Spores, safe mobile closures: [SIP-21](http://docs.scala-lang.org/sips/pending/spores.html).

Spores is an extension of the Scala compiler that enables a safer use of closures
in concurrent and distributed environments. It allows developers to guarantee properties
of functions based on types, having more control over the function's environment.

In order to provide this, spores augment the `Function` type with type information
of the captured variables. Captured variables are terms defined outside of the function
definition and used throughout the closure body. `Spores` are designed to be composable
in both logic and properties.

## Why should I use them?

Because of these features, spores are naturally useful in different programming domains. 
A classic example is function serialization where RDD-like APIs require the closure to
be serialized and sent across the wire. Spores strive to make error detection happen at
compile-time rather than runtime, and thus save time and increase developer productivity.

## About

Spores is a research project by [Heather Miller](https://heather.miller.am) and [Philipp Haller](http://lampwww.epfl.ch/~phaller/).
If you want to read more about the underlying theory, check the paper [Spores: A Type-based Foundation
for Closures in the Age of Concurrency and Distribution](https://infoscience.epfl.ch/record/191239/files/spores_1.pdf).

The Scala Center is supporting this project under the [Advisory Board proposal submitted
by IBM](https://github.com/scalacenter/advisoryboard/blob/master/proposals/006-compile-time-serializibility-check.md).
