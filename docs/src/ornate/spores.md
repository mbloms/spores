# Spores

[![Build Status](https://platform-ci.scala-lang.org/api/badges/jvican/spores-spark/status.svg)](https://platform-ci.scala-lang.org/jvican/spores-spark)

Scala Spores, safe mobile closures: [SIP-21](http://docs.scala-lang.org/sips/pending/spores.html).

Spores enable a safer use of closures in concurrent and distributed environments.
Their goal is to provide users control over functions' environment, which allows
them to control captured variables and exclude the use of certain types, among other
use cases.

Spores are naturally useful in serialization. Used with implicit-based pickling
libraries or Java Serialization, they help library authors and Spark users alike
to detect serialization issues at runtime.

More generally, spores can be used to *guarantee properties of a function*.
