package com.masterlearning.platform.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id, String type, String title, String message, String actionUrl, boolean read, LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getMessage(),
                n.getActionUrl(), n.getReadAt() != null, n.getCreatedAt());
    }
}