# Spores for Scala 3
## Usage
`sbt package` produces a jar which can be used with dotty like this:

    $ dotc -Xplugin:lib/dotty-spores.jar -classpath lib/dotty-spores.jar

## Tests
Tests are implemented using a downloaded dotty compiler and makefile scripts. 

Tests can be run either using `sbt`:

    $ sbt test

Or using `make` from `plugin-tests` directory:

    plugin-tests$ make

A specific test can be run using `make <test name>.test`.