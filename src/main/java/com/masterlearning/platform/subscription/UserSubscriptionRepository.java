package com.masterlearning.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    @Query("""
        select s from UserSubscription s
        where s.userId = :userId
          and s.status in ('PENDING', 'ACTIVE', 'PAST_DUE')
        order by s.createdAt desc
        """)
    Optional<UserSubscription> findCurrentByUserId(UUID userId);

    Optional<UserSubscription> findByExternalSubscriptionId(String externalSubscriptionId);

    List<UserSubscription> findByStatusInAndCurrentPeriodEndBefore(List<String> statuses, LocalDate date);

    List<UserSubscription> findByStatusAndCreatedAtBefore(String status, LocalDateTime date);
}
