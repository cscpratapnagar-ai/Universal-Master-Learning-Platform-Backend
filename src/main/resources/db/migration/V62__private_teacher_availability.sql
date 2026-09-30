create table if not exists private_teacher_availability (
    id uuid primary key,
    teacher_id uuid not null references users(id),
    day_of_week varchar(12) not null,
    start_time time not null,
    end_time time not null,
    timezone varchar(80) not null default 'UTC',
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0,
    constraint chk_private_teacher_availability_time check (end_time > start_time),
    constraint chk_private_teacher_availability_day check (day_of_week in ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'))
);
create index if not exists idx_private_teacher_availability_teacher on private_teacher_availability(teacher_id, active, day_of_week);
