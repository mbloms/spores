TESTS := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/test/g')
DEBUG := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/debug/g')

test-all: $(TESTS)
debug-all: $(DEBUG)


JAR := $(shell find ../target -type f -name "dotty-spores*.jar")
$(JAR): $(shell find ../src -type f)
	cd .. && sbt package

lib/dotty-spores.jar: $(JAR)
	install -D $(JAR) lib/dotty-spores.jar

#lib/dotty-spores.jar: $(shell find ../src/main -type f)
#	mkdir lib/tmp
#	dotc $(shell find ../src/main -type f -name "*.scala") -d lib/tmp
#	jar cf lib/dotty-spores.jar -C ../src/main/resources . -C lib/tmp .
#	rm -r lib/tmp

bin:
	install -d bin

result:
	install -d result

# Needed for make to understand these are not "intermediate"
RESULT := $(addprefix result/,$(shell ls expected))
prepare-result: $(RESULT)

compile := dotc -Xplugin:lib/dotty-spores.jar -classpath lib/dotty-spores.jar -d bin

result/%.err result/%.out result/%.exit: src/%.scala result bin lib/dotty-spores.jar
	@echo -e "[\033[0;34mcompiling\033[0m] $*"
	@$(compile) $< 2> result/$*.err 1> result/$*.out; echo $$? > result/$*.exit

%.test: result/%.err result/%.out result/%.exit expected/%.err expected/%.out expected/%.exit
	@diff -u {expected,result}/$*.exit || echo -e "[\033[0;31mtest failed\033[0m] $*: Exited with code $(shell cat result/$*.debug), but expected $(shell cat expected/$*.exit)"
	@diff -u {expected,result}/$*.err  || echo -e "[\033[0;31mtest failed\033[0m] $*: stderr did not match"
	@diff -u {expected,result}/$*.out  || echo -e "[\033[0;31mtest failed\033[0m] $*: stdout did not match"
	@diff -u {expected,result}/$*.err > /dev/null && diff -u {expected,result}/$*.out > /dev/null && diff -u {expected,result}/$*.exit > /dev/null
	@echo -e "[\033[0;32mtest succeeded\033[0m] $*"

%.debug result/%.debug bin/%.class: src/%.scala result bin lib/dotty-spores.jar
	$(compile) -Ylog:sporesChecker+ -Yshow-suppressed-errors -Ydebug $< ; echo $$? > result/$*.debug
	@diff -u expected/$*.exit result/$*.debug > /dev/null || (echo -e "[\033[0;31mtest failed\033[0m] $*: Exited with code $(shell cat result/$*.debug), but expected $(shell cat expected/$*.exit)"; false)