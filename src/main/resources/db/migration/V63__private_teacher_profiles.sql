create table if not exists private_teacher_profiles (
 id uuid primary key, teacher_id uuid not null unique references users(id),
 headline varchar(180), bio varchar(4000), subjects varchar(2000), teaching_modes varchar(1000),
 languages varchar(1000), hourly_rate numeric(12,2), currency varchar(3) not null default 'INR',
 accepting_learners boolean not null default false, created_at timestamptz not null default now(),
 updated_at timestamptz not null default now(), version bigint not null default 0
);
create index if not exists idx_private_teacher_profiles_active on private_teacher_profiles(accepting_learners);