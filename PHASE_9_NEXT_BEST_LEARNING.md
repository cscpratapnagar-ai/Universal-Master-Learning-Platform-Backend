# Phase 9 — Recommendation & Next-Best Learning Engine

This phase adds a deterministic, explainable recommendation layer over the existing learning signals.

## Signals
- completed lesson progress
- prerequisite graph
- question-level knowledge gaps
- learner mastery score

## Recommendation priority
1. Knowledge-gap remediation
2. Mastery building for low mastery
3. Acceleration for strong mastery
4. Normal course sequence

## API
`GET /api/v1/student/learning/enrollments/{enrollmentId}/next-best-learning`

The endpoint enforces enrollment ownership and returns the recommended lesson, recommendation type, score, explanation, and up to three alternatives.

No database migration is introduced in this phase.
