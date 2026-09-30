create table if not exists private_teacher_sessions (
 id uuid primary key,
 teacher_id uuid not null references users(id),
 learner_id uuid not null references users(id),
 starts_at timestamptz not null,
 ends_at timestamptz not null,
 timezone varchar(80) not null default 'UTC',
 status varchar(20) not null default 'REQUESTED',
 topic varchar(500),
 notes varchar(3000),
 created_at timestamptz not null default now(),
 updated_at timestamptz not null default now(),
 version bigint not null default 0,
 constraint chk_private_session_time check (ends_at > starts_at),
 constraint chk_private_session_status check (status in ('REQUESTED','CONFIRMED','CANCELLED','COMPLETED'))
);
create index if not exists idx_private_sessions_teacher_time on private_teacher_sessions(teacher_id,starts_at,ends_at,status);
create index if not exists idx_private_sessions_learner_time on private_teacher_sessions(learner_id,starts_at,ends_at,status);