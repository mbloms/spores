# Spores

[![Build Status](https://platform-ci.scala-lang.org/api/badges/jvican/spores-spark/status.svg)](https://platform-ci.scala-lang.org/jvican/spores-spark)

Scala Spores, safe mobile closures: [SIP-21](http://docs.scala-lang.org/sips/pending/spores.html).

This work is part of an *ongoing* effort to create a production-ready version
of spores compatible with Spark and Java serialization. As a novelty, it adds a compiler
plugin that complements the macro implementation. The compiler plugin performs
transitive serializable checks across `captured` elements in spores, thus preventing
users from serializing objects that would fail at runtime.

This is a [Scala Center project](https://github.com/scalacenter/advisoryboard/blob/master/proposals/006-compile-time-serializibility-check.md)
promoted and voted by the Advisory Board.

If you're interested in the theory of the project, have a look at @heathermiller and
@phaller's work: [SIP proposal](http://docs.scala-lang.org/sips/pending/spores.html) and
[paper](https://infoscience.epfl.ch/record/191239/files/spores_1.pdf).
