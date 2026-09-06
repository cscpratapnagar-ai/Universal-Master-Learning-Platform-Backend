package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.TutorConversationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TutorConversationMemory {
    private final TutorConversationRepository repository;

    public TutorConversationMemory(TutorConversationRepository repository) {
        this.repository = repository;
    }

    public List<String> recent(UUID enrollmentId) {
        return repository.recent(enrollmentId);
    }

    public void remember(UUID enrollmentId, String userMessage, String assistantResponse) {
        if (enrollmentId == null) return;
        repository.remember(enrollmentId, "LEARNER", safe(userMessage));
        repository.remember(enrollmentId, "TUTOR", safe(assistantResponse));
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.length() > 1500 ? value.substring(0, 1500) : value;
    }
}
