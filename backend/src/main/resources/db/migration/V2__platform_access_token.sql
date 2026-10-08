-- Passcode component. Contract: docs/platform-contract.md §2. Do not change without the platform owners.
create table access_token (
  id          uuid primary key,
  token_hash  text not null unique,
  label       text not null,
  expires_at  timestamptz not null,
  revoked_at  timestamptz,
  created_at  timestamptz not null default now()
);
