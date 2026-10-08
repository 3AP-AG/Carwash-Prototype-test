# Platform contract

What the platform (CAAS-1363, CAAS-1364, CAAS-1377) and this template rely on from each other.
Change it only together with the platform owners, and bump the MAJOR of `.template-version`
when a change breaks either side.

Status: **draft for agreement** (CAAS-1374). §1, §2, §8 and §9 are implemented and verified in the
template; §3–§6 wait for the platform side, §7 for the IT runbook.

## 1. Protection mode

| Item | Contract |
|---|---|
| Source | `prototype.yaml` → `protection: login \| passcode` |
| Reader | The pipeline only. It validates the value and fails the build on anything else (CAAS-1380) |
| Hand-over | Env var `PROTECTION_MODE` on `gcloud run deploy` |
| App behaviour | Refuses to start (exit 1) if `PROTECTION_MODE` is missing or invalid. There is no unprotected mode |

## 2. Tables the pipeline writes into

Both are created by the template's Flyway migrations. The platform only inserts, updates
`revoked_at`, and deletes its own run-scoped rows.

### `app_user` (login mode)

```sql
create table app_user (
  id             uuid primary key,
  username       text not null unique,
  password_hash  text not null,
  display_name   text not null,
  expires_at     timestamptz,
  created_at     timestamptz not null default now()
);
```

- `password_hash` uses Spring's `DelegatingPasswordEncoder` format, e.g. `{bcrypt}$2a$10$…`
- Run-scoped CI user (CAAS-1381): `expires_at = now() + interval '15 minutes'`, username `ci-run-<run-id>`.
  Deleted at the end of the run; unusable after expiry even if the run crashes

### `access_token` (passcode mode)

```sql
create table access_token (
  id          uuid primary key,
  token_hash  text not null unique,
  label       text not null,
  expires_at  timestamptz not null,
  revoked_at  timestamptz,
  created_at  timestamptz not null default now()
);
```

- Token: at least 128 bits from a CSPRNG, shown once, never logged unmasked
- `token_hash`: lowercase hex SHA-256 of the UTF-8 token
- `label`: neutral, e.g. `demo-1` or `ci-run-<run-id>`; never a client name
- `expires_at`: never later than `poc-expires`, the prototype's expiry label; the token job enforces this
- Run-scoped CI token (CAAS-1381): `expires_at = now() + interval '15 minutes'`, deleted at run end
- The app checks validity, expiry and `revoked_at` on every request

## 3. Reusable workflows

The template's `.github/workflows/*.yml` are thin callers of workflows in `3AP-AG/<platform-repo>`,
pinned to a major tag (`@v1`) so prototypes on an older template keep working.

| Workflow | Inputs | Outputs |
|---|---|---|
| `deploy.yml` | `protection-mode`, `image` | `candidate-url`, `failure-summary-path` |
| `passcode.yml` | `label`, `valid-days` | token (masked in the log) |

TODO(CAAS-1380, CAAS-1381): fill in the exact input and output names once the platform workflows exist.

## 4. Failure summary

Every run, gates included, writes one summary in one format so the agent reads a single shape.
The template's gates job and `make check` write the gates part; the platform writes the deploy
and acceptance part.

```
RESULT: red
STEP: lint | typecheck | test | staleness | migrations | deploy | acceptance
FAILURES:
- <backlog ID or ->  <test or rule name>  <first error line>
FULL LOG: <url or path>
```

At most a few dozen lines. TODO(CAAS-1381): agree the file name and where it is published.

## 5. Platform-set runtime settings

CAAS-1363 acceptance: "Pool settings and max-instances cannot be changed from inside a prototype repo". So the
template does **not** set them; `application.yml` only documents them.

| Setting | Value | Set by |
|---|---|---|
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | `2` | Platform deploy workflow, env var |
| `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE` | `0` | Platform deploy workflow, env var |
| `SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT` | `30s` | Platform deploy workflow, env var |
| Cloud Run `--max-instances` | `2` | Platform deploy workflow |
| Database role `CONNECTION LIMIT` | `6` | Provisioning script (CAAS-1378) |

The env vars are the normal way these values arrive, but they are not the guard: an agent could still
build its own `DataSource` in code. **The guard is `CONNECTION LIMIT 6` on the role.** A prototype
that raises its pool gets connection refusals for itself only, and other prototypes stay up
(CAAS-1363 acceptance). 2 instances × 2 connections = 4, leaving 2 for the migrations step and the
pipeline's credential inserts.

## 6. Time budget

A full run, gates through acceptance, takes at most 10 minutes, so the builder gets an answer
within one wait (CAAS-1364 acceptance). Jobs run in sequence, so their `timeout-minutes` add up to
at most 10: gates 4 (template) + deploy, migrations and acceptance 6 (platform). Starting split,
tuned on the first prototype: measure how long each step really takes, then move minutes between them.

## 7. Repo settings (IT runbook)

"Use this template" copies files only. Branch rules, required checks and repo settings are not copied,
so the IT runbook (CAAS-1384) needs these steps besides the ones it already lists:

| Step | Why |
|---|---|
| Import `.github/rulesets/main.json` as a repo ruleset | Required checks `gates` + `path-check`, admin-only override (CAAS-1382) |
| Enable "Allow auto-merge" | Merge on green with no human click (CAAS-1382) |

The path check runs on `pull_request_target` (`.github/workflows/path-check.yml`), so GitHub runs the
version on `main`. A PR can't weaken the check that judges it, and the lock needs no Enterprise feature
(3AP-AG is on GitHub Team). There is no `CODEOWNERS`: code owners need write access to the repo, which
only the builder and org admins may have (CAAS-1377). The lock is the path check plus the admin-only bypass.

## 8. The image and the migrations step (CAAS-1380)

One image per commit, built from the repo's `Dockerfile`. It runs two ways:

| Run | Command | Needs | Result |
|---|---|---|---|
| Migrations step | `<image> migrate` (e.g. a Cloud Run Job) | `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | Flyway applies `db/migration`, then the process exits: **0** on success (also when already up to date), **non-zero** on any failure |
| Web app | `<image>` | the same three, plus `PROTECTION_MODE`; `PORT` (Cloud Run sets it) | Serves frontend and API on `$PORT` (default 8080). Never migrates; the schema is validated at startup |

- The migrate run starts only a DataSource and Flyway: no web server, no app code.
- The platform never sets `SPRING_PROFILES_ACTIVE` and never sets `SPRING_FLYWAY_LOCATIONS`.
- The local seed (`backend/src/local/db`, passcode `local-passcode`) is not in the image, so no
  deployed prototype can contain it.
- The run-scoped credential (CAAS-1381) is inserted after the migrations step, into the tables in §2.

## 9. What protection looks like from outside (CAAS-1377)

The entry page is `/gate` in passcode mode and `/login` in login mode; the other one does not exist.

| Request without a session | Response |
|---|---|
| `GET /api/**` | `401`, `application/problem+json` |
| any other path (frontend, assets, deep links) | `302` to the entry page |
| `GET` the entry page | `200`, a self-contained HTML form: no script, no app text |
| `POST /gate` with a wrong, expired or revoked passcode | `401` |
| `POST /login` with a wrong password, an unknown or an expired user | `302` to `/login?error` |
| `POST` the entry page without the CSRF token | `403`, no session |
| `POST /gate` with a valid passcode | `303` to the page first asked for |
| `POST /login` with a valid user | `302` to the page first asked for |

A successful entry sets the cookie `SESSION` with `HttpOnly; Secure; SameSite=Strict`. A session is
re-checked on every request, against `access_token` (passcode) or `app_user` (login): revoking or
expiring the token, or expiring or deleting the user, ends it on the next request.
