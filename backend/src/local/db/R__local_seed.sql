-- Local profile only (`make dev`). Outside src/main, so never packaged into the image.
-- Passcode mode: passcode local-passcode (stored as its SHA-256, like every token).
-- Login mode: user local, password local-password (stored as bcrypt, like every user).
insert into access_token (id, token_hash, label, expires_at)
values ('00000000-0000-0000-0000-00000000d001', encode(sha256('local-passcode'::bytea), 'hex'), 'local',
        now() + interval '1 year')
on conflict (id) do update set expires_at = excluded.expires_at, revoked_at = null;

insert into app_user (id, username, password_hash, display_name)
values ('00000000-0000-0000-0000-00000000a001', 'local',
        '{bcrypt}$2a$10$/AcQBziddq69ClsfWwB6o.r26nDYgtA9XKTrrYuxl.X4z1wTLGJpm', 'Local user')
on conflict (id) do update set password_hash = excluded.password_hash, expires_at = null;

insert into example (id, title, status, description, created_at, updated_at) values
  ('00000000-0000-0000-0000-00000000e001', 'Example one', 'OPEN', 'The first example.',
   now() - interval '3 days', now() - interval '3 days'),
  ('00000000-0000-0000-0000-00000000e002', 'Example two', 'IN_PROGRESS', 'The second example.',
   now() - interval '2 days', now() - interval '2 days'),
  ('00000000-0000-0000-0000-00000000e003', 'Example three', 'CLOSED', '',
   now() - interval '1 day', now() - interval '1 day')
on conflict (id) do nothing;
