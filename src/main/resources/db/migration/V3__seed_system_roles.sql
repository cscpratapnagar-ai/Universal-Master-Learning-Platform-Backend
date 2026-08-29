INSERT INTO roles (code,name,description,system_role) VALUES
('STUDENT','Student','Default learner role',true),
('TEACHER','Teacher','Teaching role',true),
('ADMIN','Administrator','Platform administration role',true),
('SUPER_ADMIN','Super Administrator','Full platform access',true)
ON CONFLICT (code) DO NOTHING;