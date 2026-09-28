package com.masterlearning.platform.notification;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list() {
        return ApiResponse.success("Notifications loaded", service.list(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success("Unread notification count loaded", service.unreadCount(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable UUID id) {
        service.markRead(SecurityUtils.getCurrentUserId(), id);
        return ApiResponse.success("Notification marked as read", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        service.markAllRead(SecurityUtils.getCurrentUserId());
        return ApiResponse.success("Notifications marked as read", null);
    }
}
