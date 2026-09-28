package com.masterlearning.platform.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="billing_invoices")
public class BillingInvoice {
    @Id private UUID id;
    @Column(name="order_id", nullable=false) private UUID orderId;
    @Column(name="user_id", nullable=false) private UUID userId;
    @Column(name="invoice_number", nullable=false, unique=true, length=60) private String invoiceNumber;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Column(nullable=false, length=10) private String currency;
    @Column(nullable=false, length=30) private String status;
    @Column(name="issued_at", nullable=false) private LocalDateTime issuedAt;
    @Column(name="due_at") private LocalDateTime dueAt;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;

    protected BillingInvoice() {}
    public BillingInvoice(UUID id, UUID orderId, UUID userId, String invoiceNumber, BigDecimal amount, String currency) {
        this.id=id; this.orderId=orderId; this.userId=userId; this.invoiceNumber=invoiceNumber;
        this.amount=amount; this.currency=currency; this.status="ISSUED"; this.issuedAt=LocalDateTime.now(); this.createdAt=this.issuedAt;
    }
    public void markPaid(){status="PAID";paidAt=LocalDateTime.now();}
    public void markRefunded(){status="REFUNDED";}
    public UUID getId(){return id;} public UUID getOrderId(){return orderId;} public UUID getUserId(){return userId;}
    public String getInvoiceNumber(){return invoiceNumber;} public BigDecimal getAmount(){return amount;}
    public String getCurrency(){return currency;} public String getStatus(){return status;} public LocalDateTime getIssuedAt(){return issuedAt;}
}
