# Spores
[![Build Status](https://platform-ci.scala-lang.org/api/badges/jvican/spores-spark/status.svg)](https://platform-ci.scala-lang.org/jvican/spores-spark)

Scala Spores, safe mobile closures: [SIP-21](http://docs.scala-lang.org/sips/pending/spores.html).

Spores is an extension of the Scala compiler that enables a safer use of closures
in concurrent and distributed environments. It allows developers to guarantee properties
of functions based on types, having more control over the function's environment.

In order to provide this, spores augment the `Function` type with type information
of the captured variables. Captured variables are terms defined outside of the function
definition. `Spores` are designed to be composable: not only in their logic, but also
property-wise.

Because of these features, spores are naturally useful in different programming domains. 
A classic example would be serialization. By using implicit-based pickling libraries
or plain Java serialization, spores help developers detect serialization issues at compile-time
instead of runtime.

Spores is a research project by [Heather Miller](https://heather.miller.am) and [Philipp Haller](http://lampwww.epfl.ch/~phaller/).
If you want to read more about the underlying theory, check the paper [Spores: A Type-based Foundation
for Closures in the Age of Concurrency and Distribution](https://infoscience.epfl.ch/record/191239/files/spores_1.pdf).
