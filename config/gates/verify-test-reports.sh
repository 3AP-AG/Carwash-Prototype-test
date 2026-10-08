#!/usr/bin/env bash
# Gate: backend test reports. Locked. Run by `make test` after `mvn test`, from the repo root.
# The pom is not locked, and Maven has many ways to run fewer tests than exist (testSourceDirectory,
# a <test> property, includes, groups). Rather than chase each one, check the outcome: every test
# class in the sources has a surefire report, and no report counts a skipped test.
set -euo pipefail

src=backend/src/test/java
reports=backend/target/surefire-reports
status=0

while IFS= read -r file; do
  class=${file#"$src"/}
  class=${class%.java}
  class=${class//\//.}
  if [ ! -f "$reports/TEST-$class.xml" ]; then
    echo "No test report for $class: the build did not run it." >&2
    status=1
  fi
done < <(grep -rlE '@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b' "$src" --include='*.java')

if [ -d "$reports" ]; then
  skipped=$(grep -lE '<testsuite [^>]*skipped="[1-9]' "$reports"/TEST-*.xml || true)
  if [ -n "$skipped" ]; then
    echo "Skipped tests in (the test gate allows none):" >&2
    echo "$skipped" >&2
    status=1
  fi
fi

exit $status
