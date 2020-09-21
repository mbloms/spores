TESTS := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/test/g')
DEBUG := $(shell ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/debug/g')

test-all: $(TESTS)
debug-all: $(DEBUG)

lib/dotty-spores.jar: $(shell find ../src -type f)
	cd .. && sbt package
	install -D ../target/scala*/dotty-spores*.jar lib/dotty-spores.jar

bin:
	install -d bin

result:
	install -d result

# Needed for make to understand these are not "intermediate"
RESULT := $(addprefix result/,$(shell ls expected))
prepare-result: $(RESULT)

result/%.err result/%.out result/%.exit: src/%.scala result bin lib/dotty-spores.jar
	@echo -e "[$*]\033[50D\033[18C\033[0;34mCompiling...\033[0m"
	dotc -Xplugin:lib/dotty-spores.jar -classpath lib/dotty-spores.jar $< -d bin 2> result/$*.err 1> result/$*.out; echo $$? > result/$*.exit

%.test: result/%.err result/%.out result/%.exit expected/%.err expected/%.out expected/%.exit
	@diff -u {expected,result}/$*.err  || (echo -e "[$*]\033[50D\033[18C\033[0;31mTest failed:\033[0m stderr did not match"; false)
	@diff -u {expected,result}/$*.out  || (echo -e "[$*]\033[50D\033[18C\033[0;31mTest failed:\033[0m stdout did not match"; false)
	@diff -u {expected,result}/$*.exit || (echo -e "[$*]\033[50D\033[18C\033[0;31mTest failed:\033[0m exit code did not match"; false)
	@echo -e "[$*]\033[50D\033[18C\033[0;32mTest succeeded!\033[0m"

%.debug result/%.debug bin/%.class: src/%.scala result bin lib/dotty-spores.jar
	dotc -Xplugin:lib/dotty-spores.jar -classpath lib/dotty-spores.jar -Ylog:sporesChecker+ -Yshow-suppressed-errors -Ydebug $< -d bin; echo $$? > result/$*.debug
	@diff -u expected/$*.exit result/$*.debug || (echo -e "\033[0;31mTest failed:\033[0m exit code did not match [$*]"; false)