-- Wash appointment bookings (D-01).
create table booking (
  id              uuid primary key,
  wash_programme  text not null check (wash_programme in ('BASIC', 'COMFORT', 'PREMIUM')),
  appointment_date date not null,
  appointment_time time not null,
  customer_name   text not null,
  licence_plate   text not null,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  version         bigint not null default 0
);

create index booking_appointment_ix on booking (appointment_date, appointment_time);
