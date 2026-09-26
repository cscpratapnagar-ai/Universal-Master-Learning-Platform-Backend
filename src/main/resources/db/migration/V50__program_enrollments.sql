create table if not exists program_enrollments (
 id uuid primary key,
 program_id uuid not null references programs(id) on delete cascade,
 user_id uuid not null references users(id) on delete cascade,
 status varchar(20) not null default 'ACTIVE',
 progress_percent integer not null default 0,
 completed_at timestamptz,
 created_at timestamptz not null,
 updated_at timestamptz not null,
 constraint uk_program_enrollment_program_user unique(program_id,user_id)
);
create index if not exists idx_program_enrollment_program on program_enrollments(program_id);
create index if not exists idx_program_enrollment_user on program_enrollments(user_id);