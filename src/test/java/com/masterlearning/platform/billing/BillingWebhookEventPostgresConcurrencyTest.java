package com.masterlearning.platform.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class BillingWebhookEventPostgresConcurrencyTest {
    @Autowired
    private BillingWebhookEventRepository webhookEvents;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void concurrentDuplicateProviderEventInsertsRegisterExactlyOneRow() throws Exception {
        String eventId = "evt_race_" + UUID.randomUUID();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transactions = new TransactionTemplate(transactionManager);

        try {
            Future<Integer> first = executor.submit(() -> insertAfterBarrier(
                    transactions, ready, start, eventId, "hash-one"));
            Future<Integer> second = executor.submit(() -> insertAfterBarrier(
                    transactions, ready, start, eventId, "hash-one"));

            if (!ready.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Concurrent insert workers did not become ready");
            }
            start.countDown();

            int inserted = first.get(20, TimeUnit.SECONDS);
            int duplicate = second.get(20, TimeUnit.SECONDS);
            assertEquals(1, inserted + duplicate,
                    "Exactly one concurrent insert should create the provider event");
            Integer rows = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM billing_webhook_events WHERE provider = ? AND event_id = ?",
                    Integer.class, "RAZORPAY", eventId);
            assertEquals(1, rows);
        } finally {
            start.countDown();
            executor.shutdownNow();
            jdbcTemplate.update(
                    "DELETE FROM billing_webhook_events WHERE provider = ? AND event_id = ?",
                    "RAZORPAY", eventId);
        }
    }

    private int insertAfterBarrier(TransactionTemplate transactions, CountDownLatch ready,
                                   CountDownLatch start, String eventId, String payloadHash) throws Exception {
        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent insert start signal timed out");
        }
        return transactions.execute(status -> webhookEvents.insertIfAbsent(
                UUID.randomUUID(), "RAZORPAY", eventId, "payment.captured", payloadHash));
    }
}
