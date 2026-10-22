create or replace function prevent_private_teacher_availability_overlap()
returns trigger
language plpgsql
as $$
begin
    if new.active then
        perform pg_advisory_xact_lock(hashtextextended(new.teacher_id::text, 0));

        if exists (
            select 1
            from private_teacher_availability existing
            where existing.teacher_id = new.teacher_id
              and existing.day_of_week = new.day_of_week
              and existing.active = true
              and existing.id is distinct from new.id
              and new.start_time < existing.end_time
              and new.end_time > existing.start_time
        ) then
            raise exception 'Private teacher availability overlaps an existing active window'
                using errcode = '23P01';
        end if;
    end if;

    return new;
end;
$$;

drop trigger if exists trg_private_teacher_availability_no_overlap
    on private_teacher_availability;

create trigger trg_private_teacher_availability_no_overlap
before insert or update of teacher_id, day_of_week, start_time, end_time, active
on private_teacher_availability
for each row
execute function prevent_private_teacher_availability_overlap();
