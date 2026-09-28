package com.masterlearning.platform.subscription;

import com.masterlearning.platform.notification.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionLifecycleService {
    private final UserSubscriptionRepository subscriptions;
    private final NotificationService notifications;

    public SubscriptionLifecycleService(UserSubscriptionRepository subscriptions, NotificationService notifications) {
        this.subscriptions = subscriptions;
        this.notifications = notifications;
    }

    @Scheduled(cron = "0 15 0 * * *")
    @Transactional
    public void expireSubscriptions() {
        LocalDate today = LocalDate.now();

        var expired = subscriptions.findByStatusInAndCurrentPeriodEndBefore(
                List.of("ACTIVE", "PAST_DUE"), today);

        for (var subscription : expired) {
            subscription.expire();
            subscriptions.save(subscription);
            notifications.create(
                    subscription.getUserId(),
                    "SUBSCRIPTION_EXPIRED",
                    "Subscription expired",
                    "Your subscription period has ended. You can choose a new plan from billing.",
                    "/learner/billing"
            );
        }

        var stalePending = subscriptions.findByStatusAndCreatedAtBefore(
                "PENDING", LocalDateTime.now().minusHours(24));

        for (var subscription : stalePending) {
            subscription.expire();
            subscriptions.save(subscription);
            notifications.create(
                    subscription.getUserId(),
                    "SUBSCRIPTION_PAYMENT_EXPIRED",
                    "Pending payment expired",
                    "Your unfinished subscription payment expired. You can start a new checkout.",
                    "/learner/billing"
            );
        }
    }
}
