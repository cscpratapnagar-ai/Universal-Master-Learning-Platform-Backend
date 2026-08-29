CREATE TABLE IF NOT EXISTS system_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metadata_key VARCHAR(100) NOT NULL UNIQUE,
    metadata_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (metadata_key, metadata_value)
VALUES ('platform_name', 'Universal Master Learning Platform')
ON CONFLICT (metadata_key) DO NOTHING;