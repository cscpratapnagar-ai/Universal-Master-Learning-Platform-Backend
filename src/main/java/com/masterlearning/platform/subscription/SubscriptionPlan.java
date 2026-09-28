package com.masterlearning.platform.subscription;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", nullable = false)
    private BigDecimal yearlyPrice;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SubscriptionPlan() {}

    public UUID getId(){ return id; }
    public String getCode(){ return code; }
    public String getName(){ return name; }
    public String getDescription(){ return description; }
    public BigDecimal getMonthlyPrice(){ return monthlyPrice; }
    public BigDecimal getYearlyPrice(){ return yearlyPrice; }
    public String getCurrency(){ return currency; }
    public boolean isActive(){ return active; }
}
