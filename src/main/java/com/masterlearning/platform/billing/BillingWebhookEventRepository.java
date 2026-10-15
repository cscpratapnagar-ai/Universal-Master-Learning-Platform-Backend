package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface BillingWebhookEventRepository extends JpaRepository<BillingWebhookEvent, UUID> {
    Optional<BillingWebhookEvent> findByProviderAndEventId(String provider, String eventId);

    @Modifying
    @Query(value = """
            INSERT INTO billing_webhook_events
                (id, provider, event_id, event_type, payload_hash, status, received_at)
            VALUES
                (:id, :provider, :eventId, :eventType, :payloadHash, 'RECEIVED', CURRENT_TIMESTAMP)
            ON CONFLICT (provider, event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("provider") String provider,
                       @Param("eventId") String eventId,
                       @Param("eventType") String eventType,
                       @Param("payloadHash") String payloadHash);
}