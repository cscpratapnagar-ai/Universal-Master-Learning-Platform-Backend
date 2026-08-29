CREATE TABLE organizations (
 id UUID PRIMARY KEY, code VARCHAR(100) NOT NULL, name VARCHAR(200) NOT NULL, description VARCHAR(500), active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL, version BIGINT NOT NULL,
 CONSTRAINT uk_organizations_code UNIQUE(code)
);
CREATE TABLE organization_members (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL, user_id UUID NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL, version BIGINT NOT NULL,
 CONSTRAINT uk_organization_member UNIQUE(organization_id,user_id),
 CONSTRAINT fk_organization_members_organization FOREIGN KEY(organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
 CONSTRAINT fk_organization_members_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_organization_members_user ON organization_members(user_id);
CREATE INDEX idx_organization_members_organization ON organization_members(organization_id);