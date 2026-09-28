package com.masterlearning.platform.billing;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="billing_webhook_events", uniqueConstraints=@UniqueConstraint(name="uk_billing_webhook_provider_event", columnNames={"provider","event_id"}))
public class BillingWebhookEvent {
    @Id private UUID id;
    @Column(nullable=false,length=40) private String provider;
    @Column(name="event_id",nullable=false,length=180) private String eventId;
    @Column(name="event_type",nullable=false,length=100) private String eventType;
    @Column(name="payload_hash",nullable=false,length=128) private String payloadHash;
    @Column(nullable=false,length=30) private String status;
    @Column(name="received_at",nullable=false) private LocalDateTime receivedAt;
    @Column(name="processed_at") private LocalDateTime processedAt;
    @Column(name="error_message",length=1000) private String errorMessage;

    protected BillingWebhookEvent(){}
    public BillingWebhookEvent(String provider,String eventId,String eventType,String payloadHash){
        this.id=UUID.randomUUID();this.provider=provider;this.eventId=eventId;this.eventType=eventType;this.payloadHash=payloadHash;
        this.status="RECEIVED";this.receivedAt=LocalDateTime.now();
    }
    public void processed(){status="PROCESSED";processedAt=LocalDateTime.now();errorMessage=null;}
    public void failed(String error){status="FAILED";errorMessage=error;processedAt=LocalDateTime.now();}
    public String getStatus(){return status;}
}