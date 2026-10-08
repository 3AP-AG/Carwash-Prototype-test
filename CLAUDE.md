# Prototype · built from the 3ap prototype template (version: see `.template-version`)

This file is locked (path check). It holds the facts and limits of this template
version. How to do the work lives in the 3ap App Dev plugin.

`Example` is a style exemplar, not part of the domain: it shows the shape every real entity takes.
Copy that shape; don't build on it, change it or delete it (the plugin's skills point at it), and don't
link it in the app's navigation. Its tests, `e2e/exemplars/examples/` included, run on every candidate and
must stay green: if a change of yours breaks one, fix your change, not the test.

## Never

- Edit any path in `config/gates/locked-paths.txt` (the gates, the workflows, this file, `platform/` and the design tokens) — CI rejects it
- Weaken a gate another way: `eslint-disable`, `@ts-ignore`, `.skip`/`.only`, `@Disabled` — the gates reject these too
- Change or delete a test tagged with another backlog item's ID (`e2e/backlog/`); only the item in flight's tests may change
- Use real data, real integrations or real credentials; synthetic data only
- Put client-identifying strings in service, database or image names, labels or token labels
- Run migrations at app startup; the pipeline runs `<image> migrate` as its own step
- Serve anything without a session except the entry page (`/gate` or `/login`), or add an endpoint or page outside the chain in `platform/security`
- Hand-write API calls or edit `frontend/src/api/generated/` or `api/openapi.yaml`; run `make generate`
- Transcribe colours, spacing or type values; they come from `design-tokens/`
- Build UI outside `@/ui`, or build your own login or passcode screen
- Pool size and `max-instances` are set by the platform on deploy, not in this repo; the database caps connections anyway. Traffic shift is the platform's. Don't try to change either

## Stack

- Backend: Java 25 · Spring Boot 4 · Spring MVC on virtual threads (no WebFlux) · Postgres · Flyway · Maven wrapper
- Frontend: React 19 · Vite · TypeScript strict · TanStack Query · React Router 7 · React Hook Form + Zod · pnpm
- UI: MUI with the 3ap theme, used only inside `ui/`; features never import `@mui/*` (lint)
- One deployable: Spring Boot serves the built frontend; `/api/**` on the same origin

## Codebase map

Backend base package: `ch.aaap.prototype` (under `backend/src/main/java/`), packaged by layer.
Frontend: `frontend/src/`, packaged by feature. Paths below are relative to those roots.

| Path | What it is | Extend it when |
|---|---|---|
| `controller/` | HTTP only: maps requests to a service, returns DTOs and `PageResponse` | You add an endpoint |
| `service/` | Logic and transactions; throws the `platform/error` exceptions | You add logic |
| `repository/` | Spring Data JPA repositories | You add a query |
| `domain/` | JPA entities and their enums | You add a table |
| `dto/` | Request and response records | You add an endpoint |
| `mapper/` | Entity ↔ DTO mapping, one mapper per entity | You add a DTO |
| `config/` | The prototype's own `@Configuration`, one class per concern | You add a bean the prototype needs |
| `features/<feature>/` (frontend) | One folder per feature: pages, routes, colocated tests | You add a screen |
| `platform/passcode/` | Passcode mode: Spring-rendered `/gate`, outside the frontend bundle, styled from the tokens (`platform/web/EntryPage`) | Never; use it |
| `platform/login/` | Login mode: Spring-rendered `/login`, users from `app_user` | Never; use it |
| `platform/forms/useApiForm` (frontend) | React Hook Form + generated Zod schema + server field errors | Never; use it for every form |
| `platform/security/` | The one security chain, `PROTECTION_MODE`, session cookie | Never; every path is already covered |
| `platform/error/` | ProblemDetail errors, `NotFoundException`, `ConflictException` | Never; throw these |
| `platform/web/` | `PageResponse` (the one list shape), SPA fallback, OpenAPI shaping, `EntryPage` (the `/gate` and `/login` layout) | Never; return `PageResponse` |
| `platform/migrate/` | `<image> migrate`: Flyway as its own pipeline step | Never |
| `ui/` (`@/ui`) | The only UI components features may use: MUI wrapped in the 3ap theme (`ui/theme.ts`). `index.ts` lists them | Never from a feature |
| `design-tokens/tokens.json` (repo root) | Colours, type, spacing exported from the 3ap Figma design system → `ui/tokens/` and the `/gate`, `/login` styles | Never by hand; see `design-tokens/EXPORTED_FROM.md` |
| `api/generated/` | Typed client, Zod schemas, MSW mocks from `api/openapi.yaml`, one file set per tag | Never by hand |
| `backend/src/main/resources/db/migration/` | Flyway; next free `V<n>__<feature>.sql` | You add or change a table |
| `backend/src/local/db/` | Local seed only (passcode `local-passcode`; user `local` / `local-password`); never in the image | You need local sample data |
| `api/openapi.yaml` (repo root) | The FE/BE contract, generated from the backend | Never by hand |
| `e2e/backlog/<ID>.spec.ts` | One file per backlog item, tagged `@<ID>` | Only for the item in flight |

To add an entity: one class per layer on the backend and one folder under `features/` on the frontend,
then `make generate`. A frontend feature imports only `@/ui`, `@/api` and `@/platform`, never another feature.

## The `Example` exemplar

When you add an entity, copy the nearest exemplar exactly; don't invent a variant:

| Shape | Exemplar |
|---|---|
| Backend layers | `ExampleController` · `ExampleService` · `ExampleRepository` · `domain/Example` · `dto/Example*` · `ExampleMapper` |
| List page | `features/examples/ExampleListPage.tsx`: generated hook, loading/empty/error states, `Table`, `Pagination` with the page in the URL |
| Detail page | `features/examples/ExampleDetailPage.tsx`: 404 → "does not exist" |
| Form page | `features/examples/ExampleFormPage.tsx`: create and edit in one page, `useApiForm`, server field errors, toast, cache refresh |
| Tests | `ExampleControllerTest` · `ExampleServiceTest` · `ExampleRepositoryTest` · `mock/ExampleMock` · `features/examples/*.test.tsx` · `e2e/exemplars/examples/` |

## Code style

- Backend: google-java-format (2 spaces); `make format` fixes it, `make lint` fails on it
- Lombok, only these: `@RequiredArgsConstructor` and `@Slf4j` on services, controllers and components;
  `@Getter` and `@NoArgsConstructor(access = PROTECTED)` on entities; `@Builder` in test fixtures.
  Never `@Data`, `@Setter`, `@EqualsAndHashCode`, `@Value` or `@SneakyThrows`; DTOs are records
- Frontend: Prettier with the config in `config/gates/`; `make format` fixes it, `make lint` fails on it
- Errors: `platform/error` · lists: `PageResponse` · forms: `useApiForm` · DTO fields are always
  required in the spec · UI: `@/ui` only (lint) · API calls: generated hooks only (lint)

## Commands

| Command | Does |
|---|---|
| `make check` | lint → typecheck → test → staleness of spec, client and token files. What CI's gates run |
| `make generate` | spec from the backend code → client, Zod schemas, MSW mocks; `tokens.json` → token files |
| `make format` | rewrites Java and frontend formatting in place |
| `make lint` · `make typecheck` · `make test` | the single gates |
| `make dev` | local Postgres + migrations with the local seed + Spring + Vite on :5173 |
| `make up` · `make e2e` · `make down` | the built image locally on :8080, Playwright against it (`PROTECTION_MODE=login` for login mode) |

Run `make check` before every push; never push a known failure.
Run gates only through `make`: they use the locked configs in `config/gates/`, not `package.json` or
`pom.xml`. Needs Node 24 (`nvm use`), JDK 25+ and Docker.

## Tests

- Backend: `backend/src/test/java/`, same package as the class under test; JUnit 5, Mockito
  (`@ExtendWith(MockitoExtension.class)`), AssertJ, `// given / when / then`, `@Nested` for scenario groups
- Test data: fixtures in `mock/`, one class per entity, not built inline in each test
- Postgres for tests: one shared Testcontainers container per run, `support/PostgresContainer`; import it, never start your own
- Frontend: colocated `*.test.tsx`, `renderWithProviders` and the generated MSW handlers from `@/test/setup`
- Acceptance: `e2e/`, Chromium only, logs in once per run via `auth.setup.ts`

## This repo

- Template version: `.template-version`
- Per-prototype facts (protection mode, slug, tier, expiry): `prototype.yaml`
- Backlog: `docs/backlog.md`
