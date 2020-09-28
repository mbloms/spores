SHELL := $(shell which bash)
TESTS := $(shell find expected -name "*.exit" | sed 's/expected/result/g' | sed 's/exit/test/g')
DEBUG := $(shell find expected -name "*.exit" | sed 's/expected/result/g' | sed 's/exit/debug/g')

test-all: $(TESTS)
test-git: $(shell git ls-tree -r HEAD expected/ --name-only | grep ".exit" | sed 's/expected/result/g' | sed 's/exit/test/g')
debug-all: $(DEBUG)

dotty.mk: ../build.sbt
	cd .. && sbt dottyMk

# defines $(dottyVersion)
include dotty.mk

list-tests:
	@ls expected/*.exit | sed 's/expected\///g' | sed 's/exit/test/g'

.SECONDARY: dotc dotty-$(dottyVersion) dotty-$(dottyVersion).tar.gz

dotty-%.tar.gz:
	@echo -en "[\033[0;34mdownloading\033[0m] dotty $*: "
	curl -LO https://github.com/lampepfl/dotty/releases/download/0.27.0-RC1/$@

dotty-%: dotty-%.tar.gz
	tar -xzf $<

dotc: dotty-$(dottyVersion) bin

export PATH := dotty-$(dottyVersion)/bin:$(PATH)

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
RESULT := $(shell find expected -type f | sed 's/expected/result/g')
prepare-result: $(RESULT)

compile := dotc -Xplugin:lib/dotty-spores.jar -classpath lib/dotty-spores.jar -sourcepath src -d bin

bin/%.class: src/%.scala lib/dotty-spores.jar dotc
	@echo -e "[\033[0;34mcompiling\033[0m] $<"
	$(compile) -Yshow-suppressed-errors "$<"

expected/neg/%.exit:
	echo 1 > $@

expected/pos/%.exit:
	echo 0 > $@

expected/neg/%.out:
	touch $@

expected/%.test: src/%.scala result/%.exit result/%.err result/%.out
	install -D result/$*.exit expected/$*.exit
	@cat expected/$*.exit
	install -D result/$*.err expected/$*.err
	@cat expected/$*.err
	install -D result/$*.out expected/$*.out
	@cat expected/$*.out
	git add src/$*.scala expected/$*.{exit,err,out}

result/%.err result/%.out result/%.exit: src/%.scala lib/dotty-spores.jar dotc
	@install -d $(@D)
	@echo -e "[\033[0;34mcompiling\033[0m] $<"
	$(compile) "$<" 2> result/$*.err 1> result/$*.out; echo $$? > result/$*.exit

result/%.test: result/%.exit result/%.err result/%.out expected/%.err expected/%.out expected/%.exit
	@diff -u {expected,result}/$*.exit || echo -e "[\033[0;31mtest failed\033[0m] $*: Exited with code $$(< result/$*.exit), but expected $$(< expected/$*.exit)"
	@diff -u {expected,result}/$*.err  || echo -e "[\033[0;31mtest failed\033[0m] $*: stderr did not match"
	@diff -u {expected,result}/$*.out  || echo -e "[\033[0;31mtest failed\033[0m] $*: stdout did not match"
	@diff -u {expected,result}/$*.err > /dev/null && diff -u {expected,result}/$*.out > /dev/null && diff -u {expected,result}/$*.exit > /dev/null
	@echo -e "[\033[0;32mtest succeeded\033[0m] $*"

result/%.debug: src/%.scala lib/dotty-spores.jar dotc
	@install -d $(@D)
	@echo -e "[\033[0;34mcompiling\033[0m] $<"
	$(compile) -Ylog:sporesChecker+ -Yshow-suppressed-errors -Ydebug "$<" ; echo $$? > result/$*.debugexit
	@if test -f expected/$*.exit; then diff -u expected/$*.exit result/$*.debugexit > /dev/null || (echo -e "[\033[0;31mbad exit code\033[0m] $*: Exited with code $$(< result/$*.debugexit), but expected $$(< expected/$*.exit)"; false) && echo -e "[\033[0;32mcorrect exit code\033[0m]"; fi
	@echo -e "[\033[0;32mdebug ok\033[0m] $<"
	@rm result/$*.debugexit
