create extension if not exists btree_gist;

alter table private_teacher_sessions
  add constraint ex_private_teacher_session_overlap
  exclude using gist (
    teacher_id with =,
    tstzrange(starts_at, ends_at, '[)') with &&
  )
  where (status in ('REQUESTED','CONFIRMED'));
