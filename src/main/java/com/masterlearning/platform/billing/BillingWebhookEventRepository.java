package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BillingWebhookEventRepository extends JpaRepository<BillingWebhookEvent, UUID> {
    Optional<BillingWebhookEvent> findByProviderAndEventId(String provider, String eventId);
}