package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.GroundedTutorResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AiFallbackService {
    public GroundedTutorResponse unavailable(UUID enrollmentId, List<GroundedTutorResponse.Source> sources) {
        return new GroundedTutorResponse(
                enrollmentId,
                "The grounded AI provider is temporarily unavailable. I found relevant course material, but I will not generate an unsupported answer.",
                !sources.isEmpty(),
                false,
                sources == null ? List.of() : List.copyOf(sources));
    }

    public GroundedTutorResponse unconfigured(UUID enrollmentId, List<GroundedTutorResponse.Source> sources) {
        return new GroundedTutorResponse(
                enrollmentId,
                "Relevant course material was found, but AI generation is not configured yet. Configure the AI provider to enable grounded generation.",
                !sources.isEmpty(),
                false,
                sources == null ? List.of() : List.copyOf(sources));
    }
}
