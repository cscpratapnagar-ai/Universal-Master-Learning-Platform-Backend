package com.masterlearning.platform.modules.ai.service;

import org.springframework.stereotype.Service;

@Service
public class AiProviderGuard {
    public boolean configured(String apiKey) {
        return apiKey != null && !apiKey.trim().isBlank();
    }

    public String safeModel(String model) {
        return model == null || model.isBlank() ? "gpt-5.6-luna" : model.trim();
    }
}
