package com.masterlearning.platform.subscription;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String billingCycle;

    @Column(name = "current_period_start", nullable = false)
    private LocalDate currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDate currentPeriodEnd;

    @Column(name = "external_customer_id", length = 120)
    private String externalCustomerId;

    @Column(name = "external_subscription_id", length = 120)
    private String externalSubscriptionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserSubscription() {}

    public UserSubscription(UUID id, UUID userId, UUID planId, String status, String billingCycle,
                            LocalDate currentPeriodStart, LocalDate currentPeriodEnd) {
        this.id = id;
        this.userId = userId;
        this.planId = planId;
        this.status = status;
        this.billingCycle = billingCycle;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getPlanId() { return planId; }
    public String getStatus() { return status; }
    public String getBillingCycle() { return billingCycle; }
    public LocalDate getCurrentPeriodStart() { return currentPeriodStart; }
    public LocalDate getCurrentPeriodEnd() { return currentPeriodEnd; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public String getExternalSubscriptionId() { return externalSubscriptionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void activate(LocalDate start, LocalDate end, String externalCustomerId, String externalSubscriptionId) {
        this.status = "ACTIVE";
        this.currentPeriodStart = start;
        this.currentPeriodEnd = end;
        this.externalCustomerId = externalCustomerId;
        this.externalSubscriptionId = externalSubscriptionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPastDue() {
        this.status = "PAST_DUE";
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = "EXPIRED";
        this.updatedAt = LocalDateTime.now();
    }
}
