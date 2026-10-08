-- Login component. Contract: docs/platform-contract.md §2. Do not change without the platform owners.
create table app_user (
  id             uuid primary key,
  username       text not null unique,
  password_hash  text not null,
  display_name   text not null,
  expires_at     timestamptz,
  created_at     timestamptz not null default now()
);
