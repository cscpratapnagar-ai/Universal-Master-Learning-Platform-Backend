package com.masterlearning.platform;

import com.masterlearning.platform.billing.BillingWebhookEvent;
import com.masterlearning.platform.billing.BillingWebhookEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class MasterLearningApplicationTests {
    @Autowired
    private BillingWebhookEventRepository webhookEvents;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseRejectsDuplicateProviderEventIdentity() {
        String eventId = "evt_unique_" + UUID.randomUUID();
        webhookEvents.saveAndFlush(new BillingWebhookEvent(
                "RAZORPAY", eventId, "payment.captured", "payload-hash-one"));

        assertThrows(DataIntegrityViolationException.class, () ->
                webhookEvents.saveAndFlush(new BillingWebhookEvent(
                        "RAZORPAY", eventId, "payment.captured", "payload-hash-two")));
    }
}
