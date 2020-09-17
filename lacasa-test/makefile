
root=$(realpath ..)

#.PHONY: plugin
.INTERMEDIATE: plugin scala/spores/SporesChecker.scala
#.SECONDARY: $(RESULTS)

plugin: plugin.properties scala/spores/SporesChecker.class

plugin.properties: # makefile
	echo "pluginClass=scala.spores.SporesChecker" > plugin.properties

scala/spores: $(root)/src/main/scala/scala/spores/*.scala

scala/spores/SporesChecker.class: $(root)/src/main/scala/scala/spores/SporesChecker.scala
	dotc $(root)/src/main/scala/scala/spores/SporesChecker.scala

results:
	mkdir -p results

TESTS := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/test/g')
DEBUG := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/debug/g')

test-all: $(TESTS)

debug-all: $(DEBUG)


# Needed for make to understand these are not "intermediate"
RESULTS := $(addprefix results/,$(shell ls expected))
prepare-results: $(RESULTS)

%.test: results/%.err results/%.out results/%.exit expected/%.err expected/%.out expected/%.exit
	@diff -u {expected,results}/$*.err  || (echo -e "[$*]\t\033[0;31mTest failed:\033[0m stderr did not match"; false)
	@diff -u {expected,results}/$*.out  || (echo -e "[$*]\t\033[0;31mTest failed:\033[0m stdout did not match"; false)
	@diff -u {expected,results}/$*.exit || (echo -e "[$*]\t\033[0;31mTest failed:\033[0m exit code did not match"; false)
	@echo -e "[$*]\033[50D\033[18C\033[0;32mTest succeeded! \033[0m"

results/%.err results/%.out results/%.exit: %.scala plugin results
	dotc -Xplugin:. $< 2> results/$*.err 1> results/$*.out; echo $$? > results/$*.exit

%.debug: %.scala plugin results expected/%.exit
	dotc -Xplugin:. -Ylog:sporesChecker+ -Yshow-suppressed-errors -Ydebug $*.scala; echo $$? > results/$*.exit
	@diff -u {expected,results}/$*.exit || (echo -e "\033[0;31mTest failed:\033[0m exit code did not match [$*]"; false)

%.class: %.scala plugin
	dotc -Xplugin:. -Ylog:sporesChecker+ -Yshow-suppressed-errors -Ydebug $*.scala

%.run: %.class
	@dotr $*


# Legacy targets
test: Test.debug
test-simple: Simple.debug
test-excluded: ExcludedTest.debug

clean:
	rm -f *.{class,tasty}
	rm -rf spores
	rm -rf target
	rm -rf results