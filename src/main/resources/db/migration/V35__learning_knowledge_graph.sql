-- Knowledge graph relation layer. Explicit typed relations complement the concept hierarchy and prerequisites.
CREATE TABLE IF NOT EXISTS learning_concept_relations (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    source_concept_id UUID NOT NULL,
    target_concept_id UUID NOT NULL,
    relation_type VARCHAR(40) NOT NULL,
    weight NUMERIC(6,3) NOT NULL DEFAULT 1.000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learning_concept_relation UNIQUE (source_concept_id, target_concept_id, relation_type),
    CONSTRAINT ck_learning_concept_relation_type CHECK (relation_type IN ('RELATED_TO', 'BUILDS_ON', 'APPLIES_TO', 'GENERALIZES', 'SPECIALIZES')),
    CONSTRAINT ck_learning_concept_relation_weight CHECK (weight > 0),
    CONSTRAINT fk_learning_concept_relation_source FOREIGN KEY (source_concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_concept_relation_target FOREIGN KEY (target_concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_learning_concept_relations_course
    ON learning_concept_relations(course_id);
CREATE INDEX IF NOT EXISTS idx_learning_concept_relations_source
    ON learning_concept_relations(source_concept_id);
CREATE INDEX IF NOT EXISTS idx_learning_concept_relations_target
    ON learning_concept_relations(target_concept_id);
