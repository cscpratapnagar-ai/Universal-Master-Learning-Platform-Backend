package com.masterlearning.platform.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) {
        return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return repository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        if (repository.markRead(notificationId, userId) == 0)
            throw new IllegalArgumentException("Notification not found");
    }

    @Transactional
    public void markAllRead(UUID userId) {
        repository.markAllRead(userId);
    }

    @Transactional
    public NotificationResponse create(UUID userId, String type, String title, String message, String actionUrl) {
        return NotificationResponse.from(repository.save(
                new Notification(UUID.randomUUID(), userId, type, title, message, actionUrl)
        ));
    }
}