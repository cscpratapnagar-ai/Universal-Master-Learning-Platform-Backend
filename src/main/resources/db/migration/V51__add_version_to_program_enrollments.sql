alter table program_enrollments
    add column if not exists version bigint not null default 0;
