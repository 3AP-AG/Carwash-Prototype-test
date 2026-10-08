-- Reference entity `example`. New features take the next free V<n>__<feature>.sql.
create table example (
  id           uuid primary key,
  title        text not null,
  status       text not null check (status in ('OPEN', 'IN_PROGRESS', 'CLOSED')),
  description  text not null default '',
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now(),
  version      bigint not null default 0
);

create index example_created_at_ix on example (created_at desc);
