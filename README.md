# 3ap prototype template

GitHub template repository for the Non-Dev Prototyping Path (epic CAAS-1360, story CAAS-1361,
sub-task CAAS-1374). IT creates each prototype repo from it with "Use this template".

**Status: complete for CAAS-1374.** Every request runs through every layer: browser → passcode
gate or login → Spring → Postgres → generated spec → generated client → React. Built: the `Example`
exemplar (list, detail, create and edit, with tests on every level), both protection components
(passcode and login), the migrate mode, the gates (`make check`, CI's `gates` job, the locked-path
check), the image, and the design tokens exported from the 3ap Figma design system
(`design-tokens/`) feeding an MUI theme behind `@/ui`.

Not here, because other tickets own them: the deploy, the passcode job and the failure summary
(platform, CAAS-1380/1381), failures-only output (CAAS-1383), the backlog-ID part of the path check
(CAAS-1390). Still open: the font files and the rest of `docs/open-points.md`.

## Run it

Needs Node 24 (`nvm use`), JDK 25+ (`JAVA_HOME`) and Docker.

```sh
make install       # frontend and e2e dependencies
make check         # every gate, as CI runs it
make up            # the built image on http://localhost:8080 (passcode: local-passcode)
                   # PROTECTION_MODE=login make up → login mode (user local / local-password)
make e2e-install   # once: Chromium for Playwright
make e2e           # Playwright against :8080; login mode: E2E_USERNAME=local E2E_PASSWORD=local-password make e2e
make down          # stops it and drops the local database
make dev           # or: hot reload — Spring on :8080, Vite on http://localhost:5173
```

`POSTGRES_PORT=55432 make dev` if something else already uses port 5432.

## Reference implementation

`Example` is a placeholder entity, not a domain any prototype must use. It is a lean, complete
example of how 3ap writes a Spring Boot MVC API and a React frontend, so coding agents copy its
shape (layers, DTOs, errors, pagination, forms, tests) when they add a real entity. It stays in
every prototype, because the plugin's skills point at it; nothing is built on it, and it is not linked
in the app's navigation.

- Backend: packaged by layer (`controller`, `service`, `repository`, `domain`, `dto`, `mapper`,
  `config`), Lombok for boilerplate, google-java-format, JUnit 5 + Mockito + AssertJ
- Frontend: packaged by feature (`features/examples`), shared components only from `ui/`, API only
  through the generated client in `api/generated/`
- Template components (`platform/`), the gate configs and the design tokens are locked
  (`config/gates/locked-paths.txt`); prototypes use them, never change them. `ui/` is the only UI
  a feature may use, but apart from its tokens it isn't locked

| Read | For |
|---|---|
| `CLAUDE.md` | What every agent session in a prototype repo is told (rules and limits, codebase map) |
| `docs/platform-contract.md` | What the platform and the template rely on from each other |
| `docs/open-points.md` | What is still undecided |

Seam with CAAS-1375 (plugin skills) and CAAS-1377 (protection suite): the paths in the
`CLAUDE.md` codebase map, the component names `platform/login` and `platform/passcode`,
`api/openapi.yaml`, `frontend/src/api/generated/` and the `make` targets.
