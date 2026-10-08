# config/gates/: the gate definitions

Locked: listed in `locked-paths.txt` and enforced by the path check. The `Makefile` calls
these files **directly**, never `package.json` scripts or configs the agent can edit, so no edit
elsewhere in the repo can weaken a gate.

| File | Gate | Closes |
|---|---|---|
| `eslint.config.js` | lint (frontend, e2e) | run with `--no-inline-config`: no `eslint-disable` comments; `ban-ts-comment` blocks `@ts-ignore`; no skipped/focused tests |
| `prettier.config.js`, `.prettierignore` | lint (format) | generated code is excluded; patterns are relative to the ignore file |
| `tsconfig.gate.json`, `tsconfig.e2e.json` | typecheck (frontend, e2e) | self-contained strict settings; does not extend the editable `frontend/tsconfig.json` |
| `vitest.gate.config.ts` | test (frontend) | fixes include/exclude, environment, setup, `passWithNoTests: false` and `allowOnly: false` over whatever `frontend/vite.config.ts` says |
| `checkstyle.xml` | lint (backend) | run as a pinned standalone jar (`Makefile`, SHA-256 checked), so the pom can't skip or change it; covers main and test sources; bans `@Disabled`, WebFlux imports and Lombok annotations outside the allowed set |
| google-java-format (version and SHA-256 pinned in the `Makefile`) | lint (backend format) | run as a standalone jar, so the pom can't change it; `make format` applies it |
| `package.json` | — | marks the configs as ES modules; nothing else |
| `reporters/` | all | failures-only output where no built-in reporter does that; empty until CAAS-1383 |
| `locked-paths.txt`, `path_check.py` | path check | the one list of locked paths, and the script `path-check.yml` runs against the PR's changed files |

`backend/pom.xml` stays editable (dependencies), but it must not configure test execution: explicit
plugin configuration beats the `-DskipTests=false -Dmaven.test.skip=false` the `Makefile` passes, so
`make test` fails if the pom mentions surefire, failsafe, `skipTests`, `testFailureIgnore` or
`maven.test.*`. The Maven wrapper (`backend/mvnw`, `backend/.mvn/`) is locked.
