package com.masterlearning.platform.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="billing_refunds")
public class BillingRefund {
    @Id private UUID id;
    @Column(name="payment_id",nullable=false) private UUID paymentId;
    @Column(name="order_id",nullable=false) private UUID orderId;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(nullable=false,length=40) private String provider;
    @Column(name="provider_refund_id",length=120) private String providerRefundId;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
    @Column(nullable=false,length=10) private String currency;
    @Column(nullable=false,length=30) private String status;
    @Column(length=500) private String reason;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;

    protected BillingRefund(){}
    public BillingRefund(UUID id,UUID paymentId,UUID orderId,UUID userId,String provider,BigDecimal amount,String currency,String reason){
        this.id=id;this.paymentId=paymentId;this.orderId=orderId;this.userId=userId;this.provider=provider;this.amount=amount;this.currency=currency;this.reason=reason;
        this.status="REQUESTED";this.createdAt=LocalDateTime.now();this.updatedAt=this.createdAt;
    }
    public void refunded(String providerRefundId){this.providerRefundId=providerRefundId;this.status="REFUNDED";this.updatedAt=LocalDateTime.now();}
    public void failed(){this.status="FAILED";this.updatedAt=LocalDateTime.now();}
    public UUID getId(){return id;} public UUID getPaymentId(){return paymentId;} public UUID getOrderId(){return orderId;}
    public UUID getUserId(){return userId;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;}
    public String getStatus(){return status;} public String getProviderRefundId(){return providerRefundId;}
}
