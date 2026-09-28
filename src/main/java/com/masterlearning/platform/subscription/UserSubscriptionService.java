package com.masterlearning.platform.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import com.masterlearning.platform.notification.NotificationService;

@Service
public class UserSubscriptionService {
    private final UserSubscriptionRepository subscriptions;
    private final SubscriptionPlanRepository plans;
    private final NotificationService notifications;

    public UserSubscriptionService(UserSubscriptionRepository subscriptions, SubscriptionPlanRepository plans, NotificationService notifications) {
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public UserSubscriptionResponse current(UUID userId) {
        return subscriptions.findCurrentByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> freeResponse());
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentPaidSubscription(UUID userId) {
        return subscriptions.findCurrentByUserId(userId)
                .map(subscription -> !"FREE".equalsIgnoreCase(
                        plans.findById(subscription.getPlanId()).map(SubscriptionPlan::getCode).orElse("FREE")))
                .orElse(false);
    }

    @Transactional
    public UserSubscription createPending(UUID userId, String planCode, String billingCycle) {
        var existing = subscriptions.findCurrentByUserId(userId);
        if (existing.isPresent()) {
            throw new IllegalStateException("User already has a current subscription");
        }

        var plan = plans.findByCode(planCode.toUpperCase())
                .filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active subscription plan not found: " + planCode));

        String cycle = billingCycle == null ? "MONTHLY" : billingCycle.toUpperCase();
        if (!cycle.equals("MONTHLY") && !cycle.equals("YEARLY")) {
            throw new IllegalArgumentException("Billing cycle must be MONTHLY or YEARLY");
        }

        LocalDate start = LocalDate.now();
        LocalDate end = cycle.equals("YEARLY") ? start.plusYears(1).minusDays(1) : start.plusMonths(1).minusDays(1);

        return subscriptions.save(new UserSubscription(
                UUID.randomUUID(),
                userId,
                plan.getId(),
                "PENDING",
                cycle,
                start,
                end
        ));
    }

    @Transactional
    public UserSubscription activatePending(UUID userId, String planCode, String billingCycle, LocalDate start, LocalDate end, String externalSubscriptionId) {
        var subscription = subscriptions.findCurrentByUserId(userId).orElseGet(() -> createPending(userId, planCode, billingCycle));
        var plan = plans.findByCode(planCode.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));
        if (!subscription.getPlanId().equals(plan.getId())) {
            throw new IllegalStateException("Pending subscription does not match paid plan");
        }
        subscription.activate(start, end, null, externalSubscriptionId);
        var saved = subscriptions.save(subscription);
        notifications.create(userId, "SUBSCRIPTION_ACTIVATED", "Subscription activated",
                "Your " + plan.getName() + " subscription is now active until " + end + ".",
                "/learner/billing");
        return saved;
    }

    @Transactional
    public UserSubscriptionResponse cancelForRefund(UUID userId) {
        var subscription = subscriptions.findCurrentByUserId(userId).orElse(null);
        if (subscription == null) {
            return freeResponse();
        }
        if ("CANCELLED".equals(subscription.getStatus()) || "EXPIRED".equals(subscription.getStatus())) {
            return toResponse(subscription);
        }
        subscription.cancel();
        var saved = subscriptions.save(subscription);
        notifications.create(userId, "SUBSCRIPTION_REFUNDED", "Subscription ended after refund",
                "Your paid subscription was ended because the payment was fully refunded.",
                "/learner/billing");
        return toResponse(saved);
    }

    @Transactional
    public UserSubscriptionResponse cancel(UUID userId) {
        var subscription = subscriptions.findCurrentByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No current subscription found"));
        if ("CANCELLED".equals(subscription.getStatus()) || "EXPIRED".equals(subscription.getStatus())) {
            return toResponse(subscription);
        }
        subscription.cancel();
        var saved = subscriptions.save(subscription);
        notifications.create(userId, "SUBSCRIPTION_CANCELLED", "Subscription cancelled",
                "Your " + plans.findById(subscription.getPlanId()).map(SubscriptionPlan::getName).orElse("subscription") + " subscription has been cancelled.",
                "/learner/billing");
        return toResponse(saved);
    }

    private UserSubscriptionResponse toResponse(UserSubscription subscription) {
        var plan = plans.findById(subscription.getPlanId())
                .orElseThrow(() -> new IllegalStateException("Subscription plan not found"));
        boolean active = subscription.getStatus().equals("ACTIVE")
                && !subscription.getCurrentPeriodEnd().isBefore(LocalDate.now());
        return new UserSubscriptionResponse(
                plan.getCode(),
                plan.getName(),
                subscription.getStatus(),
                subscription.getBillingCycle(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                active
        );
    }

    private UserSubscriptionResponse freeResponse() {
        var free = plans.findByCode("FREE")
                .orElseThrow(() -> new IllegalStateException("FREE subscription plan is not configured"));
        return new UserSubscriptionResponse(
                free.getCode(),
                free.getName(),
                "FREE",
                "MONTHLY",
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1),
                true
        );
    }
}
