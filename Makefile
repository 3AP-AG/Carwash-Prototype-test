# Single entry point for humans, the agent and CI. Locked (config/gates/locked-paths.txt).
# Every gate calls its locked config in config/gates/ directly, never a package.json script and
# never a setting in backend/pom.xml, so no edit elsewhere in the repo can weaken a gate.
# Needs: Node 24 (`nvm use`), JDK 25+ (JAVA_HOME), Docker (Testcontainers, local Postgres).
# TODO(CAAS-1383): failures-only output with VERBOSE=1 for the full log; for now targets print
# what the tools print.

SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c

PNPM := corepack pnpm
MVN := ./mvnw -B -q
GATES := ../config/gates
CACHE := .gates-cache
JAVA := $(if $(JAVA_HOME),$(JAVA_HOME)/bin/java,java)
JAVA_SOURCES = $(shell find backend/src -name '*.java')
GENERATED := api/openapi.yaml frontend/src/api/generated frontend/src/ui/tokens/tokens.css frontend/src/ui/tokens/tokens.ts \
             backend/src/main/resources/platform/entry-tokens.css

# Backend lint and format run as pinned standalone jars (SHA-256 checked), independent of
# backend/pom.xml. Downloaded once into $(CACHE).
CHECKSTYLE_VERSION := 14.3.0
CHECKSTYLE_SHA256 := 754e218ab1fcabb1e1c5f8530e9d3aa37636806c22987bb279dd907a6ee749b2
CHECKSTYLE_JAR := $(CACHE)/checkstyle-$(CHECKSTYLE_VERSION)-all.jar
GJF_VERSION := 1.37.0
GJF_SHA256 := 834b2a0c38cb774953322a84b5ca3f2f40dd3156650b3cd44d3b744345962f7a
GJF_JAR := $(CACHE)/google-java-format-$(GJF_VERSION)-all-deps.jar

export COREPACK_ENABLE_DOWNLOAD_PROMPT := 0

.PHONY: install generate spec client tokens format lint typecheck test check e2e e2e-install dev up down image node-version

node-version:
	@node -v | grep -qE '^v24\.' || { echo "Node 24 required, found $$(node -v). Run: nvm use"; exit 1; }

install: node-version  ## install frontend and e2e dependencies from the lockfiles
	cd frontend && $(PNPM) install --frozen-lockfile
	cd e2e && $(PNPM) install --frozen-lockfile

generate: spec client tokens  ## spec from the backend code → typed client, Zod schemas, MSW mocks; tokens → CSS

spec:
	cd backend && $(MVN) test -Dtest=OpenApiSpecExportTest -Dopenapi.export=true -Dsurefire.failIfNoSpecifiedTests=false

client: node-version
	cd frontend && $(PNPM) exec orval --config orval.config.ts > /dev/null

tokens: node-version  ## design-tokens/tokens.json (the Figma export) → frontend/src/ui/tokens/tokens.{css,ts} and the /gate, /login entry-tokens.css
	cd frontend && $(PNPM) exec style-dictionary build --silent --config ../design-tokens/style-dictionary.config.mjs

format: node-version $(GJF_JAR)  ## rewrite Java (google-java-format) and frontend (Prettier) in place
	$(JAVA) -jar $(GJF_JAR) --replace $(JAVA_SOURCES)
	cd frontend && $(PNPM) exec prettier --write --log-level warn --config $(GATES)/prettier.config.js --ignore-path $(GATES)/.prettierignore .

lint: node-version $(CHECKSTYLE_JAR) $(GJF_JAR)  ## ESLint (frontend, e2e) + Prettier (frontend), google-java-format + Checkstyle (backend)
	frontend/node_modules/.bin/eslint -c config/gates/eslint.config.js --no-inline-config frontend e2e
	cd frontend && $(PNPM) exec prettier --check --log-level warn --config $(GATES)/prettier.config.js --ignore-path $(GATES)/.prettierignore .
	@$(JAVA) -jar $(GJF_JAR) --dry-run --set-exit-if-changed $(JAVA_SOURCES) \
	  || { echo "Java formatting differs (files above). Run: make format"; exit 1; }
	$(JAVA) -jar $(CHECKSTYLE_JAR) -c config/gates/checkstyle.xml backend/src/main/java backend/src/test/java > /dev/null

typecheck: node-version  ## tsc against the locked tsconfigs (frontend, e2e); javac for main and test sources
	cd frontend && $(PNPM) exec tsc -p $(GATES)/tsconfig.gate.json
	cd frontend && $(PNPM) exec tsc -p $(GATES)/tsconfig.e2e.json
	cd backend && $(MVN) test-compile

EXPORT_SPEC ?= false
test: node-version  ## Vitest (frontend), JUnit + Testcontainers (backend)
	cd frontend && $(PNPM) exec vitest run -c $(GATES)/vitest.gate.config.ts
	@# Explicit plugin configuration in the pom beats -D flags, so the pom must not touch test execution.
	@if grep -nE 'maven-(surefire|failsafe)-plugin|skipTests|testFailureIgnore|maven\.test\.' backend/pom.xml; then \
	  echo "backend/pom.xml must not configure test execution (lines above); the test gate owns it."; exit 1; fi
	rm -rf backend/target/surefire-reports
	cd backend && $(MVN) test -DskipTests=false -Dmaven.test.skip=false -Dopenapi.export=$(EXPORT_SPEC)
	config/gates/verify-test-reports.sh

# Run before every push; CI's gates job runs the same. Cheapest first, stop at the first failure.
# The last step regenerates spec, client and token files and fails if that changed anything, i.e. if the
# committed (or about-to-be-committed) output is stale.
# TODO(CAAS-1383): the path check against config/gates/locked-paths.txt, so a locked-path change
# fails locally before CI's path check rejects it.
check:
	@mkdir -p $(CACHE)
	@$(MAKE) --no-print-directory _generated-hash > $(CACHE)/generated.before
	@$(MAKE) --no-print-directory lint
	@$(MAKE) --no-print-directory typecheck
	@$(MAKE) --no-print-directory test EXPORT_SPEC=true
	@$(MAKE) --no-print-directory client
	@$(MAKE) --no-print-directory tokens
	@$(MAKE) --no-print-directory _generated-hash > $(CACHE)/generated.after
	@cmp -s $(CACHE)/generated.before $(CACHE)/generated.after || { \
	  echo "STALE: api/openapi.yaml, frontend/src/api/generated or the token files did not match their source."; \
	  echo "make check has regenerated them; review and commit the result."; exit 1; }
	@echo "make check: all gates passed"

_generated-hash:
	@find $(GENERATED) -type f | LC_ALL=C sort | xargs shasum -a 256

$(CHECKSTYLE_JAR):
	@mkdir -p $(CACHE)
	curl -sSfL -o $@.tmp https://github.com/checkstyle/checkstyle/releases/download/checkstyle-$(CHECKSTYLE_VERSION)/checkstyle-$(CHECKSTYLE_VERSION)-all.jar
	echo "$(CHECKSTYLE_SHA256)  $@.tmp" | shasum -a 256 -c - > /dev/null
	mv $@.tmp $@

$(GJF_JAR):
	@mkdir -p $(CACHE)
	curl -sSfL -o $@.tmp https://github.com/google/google-java-format/releases/download/v$(GJF_VERSION)/google-java-format-$(GJF_VERSION)-all-deps.jar
	echo "$(GJF_SHA256)  $@.tmp" | shasum -a 256 -c - > /dev/null
	mv $@.tmp $@

e2e-install: node-version  ## download the Chromium build Playwright uses
	cd e2e && $(PNPM) exec playwright install chromium

BASE_URL ?= http://localhost:8080
E2E_PASSCODE ?= local-passcode
# Login mode: E2E_USERNAME=local E2E_PASSWORD=local-password make e2e
e2e: node-version  ## Playwright against BASE_URL (default: the local container from `make up`)
	cd e2e && BASE_URL=$(BASE_URL) E2E_PASSCODE=$(E2E_PASSCODE) E2E_USERNAME=$(E2E_USERNAME) E2E_PASSWORD=$(E2E_PASSWORD) $(PNPM) exec playwright test

# migrate: the profile goes in the environment; -Dspring-boot.run.profiles would put it before `migrate`.
dev: node-version  ## local Postgres + migrations with the local seed + Spring (local profile) + Vite on :5173
	docker compose up -d --wait postgres
	cd backend && SPRING_PROFILES_ACTIVE=local $(MVN) spring-boot:run -Dspring-boot.run.arguments=migrate
	trap 'kill 0' EXIT; \
	  (cd backend && $(MVN) spring-boot:run -Dspring-boot.run.profiles=local) & \
	  (cd frontend && $(PNPM) dev) & \
	  wait

image:  ## build the deployable image
	docker build -t prototype:local .

up:  ## the built image locally: Postgres, `migrate` (with the local seed), app on :8080; PROTECTION_MODE=login for login mode
	docker compose --profile app up -d --build
	@for i in $$(seq 1 60); do \
	  [ "$$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/)" = "302" ] && exit 0; sleep 1; done; \
	  echo "app did not come up on :8080"; docker compose --profile app logs app | tail -30; exit 1

down:
	docker compose --profile app down -v
