-- Local development demo accounts only. This location is configured exclusively by application-local.yml.
-- All demo users use the known local-only password "password"; never deploy these credentials to production.
INSERT INTO users (id, email, password_hash, first_name, last_name, enabled, created_at, updated_at, version)
VALUES
('20000000-0000-0000-0000-000000000001','superadmin@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Platform','Super Admin',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('30000000-0000-0000-0000-000000000001','student@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Test','Student',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000001','orgadmin@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Organization Admin',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000002','teacher@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Teacher',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000003','tutor@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Private Tutor',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000004','examiner@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Examiner',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000005','paper.setter@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Question Setter',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000006','evaluator@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Evaluator',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000007','proctor@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Proctor',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000008','parent@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Parent',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000009','finance@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Finance',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0),
('71000000-0000-0000-0000-000000000010','support@masterlearning.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Demo','Support',TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    enabled = TRUE,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES
 ('superadmin@masterlearning.local','SUPER_ADMIN'),
 ('student@masterlearning.local','STUDENT'),
 ('orgadmin@masterlearning.local','ORG_ADMIN'),
 ('teacher@masterlearning.local','TEACHER'),
 ('tutor@masterlearning.local','PRIVATE_TEACHER'),
 ('examiner@masterlearning.local','EXAMINER'),
 ('paper.setter@masterlearning.local','QUESTION_SETTER'),
 ('evaluator@masterlearning.local','EVALUATOR'),
 ('proctor@masterlearning.local','PROCTOR'),
 ('parent@masterlearning.local','PARENT'),
 ('finance@masterlearning.local','FINANCE'),
 ('support@masterlearning.local','SUPPORT')
) AS demo(email, role_code)
JOIN users u ON LOWER(u.email) = demo.email
JOIN roles r ON r.code = demo.role_code
ON CONFLICT DO NOTHING;
