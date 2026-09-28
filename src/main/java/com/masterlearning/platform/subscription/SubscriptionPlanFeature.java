package com.masterlearning.platform.subscription;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "subscription_plan_features")
public class SubscriptionPlanFeature {
    @Id
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "feature_code", nullable = false, length = 80)
    private String featureCode;

    @Column(name = "feature_value", length = 200)
    private String featureValue;

    protected SubscriptionPlanFeature() {}

    public UUID getId(){ return id; }
    public UUID getPlanId(){ return planId; }
    public String getFeatureCode(){ return featureCode; }
    public String getFeatureValue(){ return featureValue; }
}
