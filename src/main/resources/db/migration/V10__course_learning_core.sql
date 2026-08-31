CREATE TABLE courses (
    id UUID PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(220) UNIQUE,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    organization_id UUID REFERENCES organizations(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_organization ON courses(organization_id);
