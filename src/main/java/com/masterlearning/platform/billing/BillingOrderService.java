package com.masterlearning.platform.billing;

import com.masterlearning.platform.subscription.SubscriptionPlan;
import com.masterlearning.platform.subscription.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BillingOrderService {
    private final BillingOrderRepository orders;
    private final BillingPaymentRepository payments;
    private final SubscriptionPlanRepository plans;

    public BillingOrderService(BillingOrderRepository orders, BillingPaymentRepository payments, SubscriptionPlanRepository plans) {
        this.orders = orders;
        this.payments = payments;
        this.plans = plans;
    }

    @Transactional
    public BillingOrderResponse create(UUID userId, CreateBillingOrderRequest request) {
        if (request == null || request.planCode() == null || request.planCode().isBlank()) {
            throw new IllegalArgumentException("Plan code is required");
        }

        var plan = plans.findByCode(request.planCode().trim().toUpperCase())
                .filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active subscription plan not found"));

        String cycle = request.billingCycle() == null ? "MONTHLY" : request.billingCycle().trim().toUpperCase();
        if (!cycle.equals("MONTHLY") && !cycle.equals("YEARLY")) {
            throw new IllegalArgumentException("Billing cycle must be MONTHLY or YEARLY");
        }

        BigDecimal amount = cycle.equals("YEARLY") ? plan.getYearlyPrice() : plan.getMonthlyPrice();
        if (amount.signum() < 0) {
            throw new IllegalStateException("Plan price cannot be negative");
        }

        var order = orders.save(new BillingOrder(
                UUID.randomUUID(),
                userId,
                plan.getId(),
                cycle,
                amount,
                plan.getCurrency()
        ));

        order.markPendingPayment();
        orders.save(order);

        payments.save(new BillingPayment(
                UUID.randomUUID(),
                order.getId(),
                "PENDING_PROVIDER",
                amount,
                plan.getCurrency()
        ));

        return new BillingOrderResponse(
                order.getId(),
                plan.getCode(),
                plan.getName(),
                cycle,
                amount,
                plan.getCurrency(),
                order.getStatus(),
                "PENDING_PROVIDER"
        );
    }
}
