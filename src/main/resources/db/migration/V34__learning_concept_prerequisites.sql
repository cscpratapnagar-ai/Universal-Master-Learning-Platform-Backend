CREATE TABLE IF NOT EXISTS learning_concept_prerequisites (
    concept_id UUID NOT NULL,
    prerequisite_concept_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (concept_id, prerequisite_concept_id),
    CONSTRAINT ck_learning_concept_prerequisite_not_self CHECK (concept_id <> prerequisite_concept_id),
    CONSTRAINT fk_learning_concept_prerequisite_concept FOREIGN KEY (concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_concept_prerequisite_prerequisite FOREIGN KEY (prerequisite_concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_learning_concept_prerequisites_prerequisite
    ON learning_concept_prerequisites(prerequisite_concept_id);
