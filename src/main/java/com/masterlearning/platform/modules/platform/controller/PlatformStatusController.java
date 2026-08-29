package com.masterlearning.platform.modules.platform.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PlatformStatusController {

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.success(
                "Platform is online",
                Map.of(
                        "status", "ONLINE",
                        "service", "Universal Master Learning Platform",
                        "timestamp", Instant.now().toString()
                )
        );
    }
}
