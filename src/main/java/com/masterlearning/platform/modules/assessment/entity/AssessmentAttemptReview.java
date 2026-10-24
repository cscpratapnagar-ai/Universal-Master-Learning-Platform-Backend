package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_attempt_reviews",
       uniqueConstraints = @UniqueConstraint(name = "uk_assessment_attempt_review_attempt", columnNames = "attempt_id"))
public class AssessmentAttemptReview extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt = Instant.now();

    protected AssessmentAttemptReview() {}

    public AssessmentAttemptReview(AssessmentAttempt attempt, User reviewer, String remarks) {
        this.attempt = attempt;
        this.reviewer = reviewer;
        this.remarks = remarks.trim();
        this.reviewedAt = Instant.now();
    }

    public void updateRemarks(User reviewer, String remarks) {
        this.reviewer = reviewer;
        this.remarks = remarks.trim();
        this.reviewedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public User getReviewer() { return reviewer; }
    public String getRemarks() { return remarks; }
    public Instant getReviewedAt() { return reviewedAt; }
}