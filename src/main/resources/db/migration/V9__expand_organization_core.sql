ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS slug VARCHAR(120),
    ADD COLUMN IF NOT EXISTS legal_name VARCHAR(250),
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS organization_type VARCHAR(80),
    ADD COLUMN IF NOT EXISTS registration_number VARCHAR(120),
    ADD COLUMN IF NOT EXISTS established_date DATE,
    ADD COLUMN IF NOT EXISTS primary_email VARCHAR(254),
    ADD COLUMN IF NOT EXISTS primary_phone VARCHAR(40),
    ADD COLUMN IF NOT EXISTS alternate_phone VARCHAR(40),
    ADD COLUMN IF NOT EXISTS website VARCHAR(500),
    ADD COLUMN IF NOT EXISTS address_line VARCHAR(500),
    ADD COLUMN IF NOT EXISTS country VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state VARCHAR(100),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS district VARCHAR(100),
    ADD COLUMN IF NOT EXISTS postal_code VARCHAR(30),
    ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS primary_color VARCHAR(20),
    ADD COLUMN IF NOT EXISTS secondary_color VARCHAR(20),
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

UPDATE organizations
SET display_name = name,
    legal_name = name,
    status = CASE WHEN active THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE display_name IS NULL OR legal_name IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_organizations_slug ON organizations (slug)
WHERE slug IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_organizations_status ON organizations (status);
CREATE INDEX IF NOT EXISTS idx_organizations_type ON organizations (organization_type);
