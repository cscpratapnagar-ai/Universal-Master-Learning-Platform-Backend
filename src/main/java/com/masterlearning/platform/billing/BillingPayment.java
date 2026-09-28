package com.masterlearning.platform.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="billing_payments")
public class BillingPayment {
    @Id private UUID id;
    @Column(name="order_id",nullable=false) private UUID orderId;
    @Column(nullable=false,length=40) private String provider;
    @Column(name="provider_payment_id",length=120) private String providerPaymentId;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
    @Column(nullable=false,length=10) private String currency;
    @Column(nullable=false,length=30) private String status;
    @Column(name="failure_reason",length=500) private String failureReason;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;

    protected BillingPayment(){}
    public BillingPayment(UUID id,UUID orderId,String provider,BigDecimal amount,String currency){
        this.id=id;this.orderId=orderId;this.provider=provider;this.amount=amount;this.currency=currency;
        this.status="CREATED";this.createdAt=LocalDateTime.now();this.updatedAt=this.createdAt;
    }
    public UUID getId(){return id;} public UUID getOrderId(){return orderId;} public String getProvider(){return provider;}
    public String getProviderPaymentId(){return providerPaymentId;} public String getFailureReason(){return failureReason;}
    public void capture(String providerPaymentId){this.providerPaymentId=providerPaymentId;this.status="CAPTURED";this.paidAt=LocalDateTime.now();this.updatedAt=LocalDateTime.now();}
    public void fail(String reason){this.status="FAILED";this.failureReason=reason;this.updatedAt=LocalDateTime.now();}
    public void refund(){this.status="REFUNDED";this.updatedAt=LocalDateTime.now();}
    public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public String getStatus(){return status;}
}
