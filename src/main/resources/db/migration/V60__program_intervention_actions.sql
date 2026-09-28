create table if not exists program_intervention_actions (
    id uuid primary key,
    program_id uuid not null references programs(id),
    user_id uuid not null references users(id),
    action_type varchar(60) not null,
    status varchar(20) not null default 'OPEN',
    note varchar(3000),
    actor varchar(180) not null,
    resolved_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0
);
create index if not exists idx_intervention_program_user on program_intervention_actions(program_id,user_id,created_at);
