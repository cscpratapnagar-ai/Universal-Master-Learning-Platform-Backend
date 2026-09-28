package com.masterlearning.platform.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "billing_orders")
public class BillingOrder {
    @Id private UUID id;
    @Column(name="user_id", nullable=false) private UUID userId;
    @Column(name="plan_id", nullable=false) private UUID planId;
    @Column(name="billing_cycle", nullable=false, length=20) private String billingCycle;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Column(nullable=false, length=10) private String currency;
    @Column(nullable=false, length=30) private String status;
    @Column(name="external_order_id", length=120) private String externalOrderId;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;

    protected BillingOrder() {}

    public BillingOrder(UUID id, UUID userId, UUID planId, String billingCycle, BigDecimal amount, String currency) {
        this.id=id; this.userId=userId; this.planId=planId; this.billingCycle=billingCycle;
        this.amount=amount; this.currency=currency; this.status="CREATED";
        this.createdAt=LocalDateTime.now(); this.updatedAt=this.createdAt;
    }

    public UUID getId(){return id;} public UUID getUserId(){return userId;} public UUID getPlanId(){return planId;}
    public String getBillingCycle(){return billingCycle;} public BigDecimal getAmount(){return amount;}
    public String getCurrency(){return currency;} public String getStatus(){return status;}
    public String getExternalOrderId(){return externalOrderId;} public LocalDateTime getCreatedAt(){return createdAt;}
    public void setExternalOrderId(String externalOrderId){this.externalOrderId=externalOrderId;this.updatedAt=LocalDateTime.now();}
    public void markPendingPayment(){status="PENDING_PAYMENT";updatedAt=LocalDateTime.now();}
    public void markPaid(){status="PAID";updatedAt=LocalDateTime.now();}
    public void markFailed(){status="FAILED";updatedAt=LocalDateTime.now();}
    public void cancel(){status="CANCELLED";updatedAt=LocalDateTime.now();}
}
