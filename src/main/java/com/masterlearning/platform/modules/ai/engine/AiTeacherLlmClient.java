package com.masterlearning.platform.modules.ai.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.modules.ai.dto.request.AiTeacherTurnRequest;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherTurnResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AiTeacherLlmClient {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public AiTeacherLlmClient(ObjectMapper mapper,
                              @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
                              @Value("${app.ai.openai.api-key:}") String apiKey,
                              @Value("${app.ai.openai.model:gpt-5.6-luna}") String model) {
        this.mapper = mapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Optional<AiTeacherTurnResponse> teach(AiTeacherTurnRequest request) {
        if (apiKey.isBlank()) return Optional.empty();
        String topic = safe(request == null ? null : request.topic(), "today's topic");
        String language = safe(request == null ? null : request.language(), "EN");
        String phase = safe(request == null ? null : request.phase(), "INTRO");
        int minute = request == null || request.lectureMinute() == null ? 0 : request.lectureMinute();
        String studentMessage = safe(request == null ? null : request.studentMessage(), "(no student answer yet)");

        String prompt = """
                You are the AI Teacher inside the Master Learning System.
                Teach like an excellent human teacher, not like a generic chatbot.
                Stay on the requested topic. Adapt difficulty from the student's answer.
                If the student is confused, simplify and give a hint before the answer.
                Return ONLY one valid JSON object with exactly these keys:
                phase, teacherText, teachingMode, visualMode, nextPhase, askStudent, studentPrompt, lectureComplete.
                Allowed phases: INTRO, EXPLAIN, EXAMPLE, CHECK, PRACTICE, RECAP, COMPLETE.
                Keep teacherText concise but useful (normally 2-5 sentences).
                studentPrompt should be empty when askStudent is false.
                lectureComplete must be true only in RECAP or COMPLETE.
                language: %s
                topic: %s
                currentPhase: %s
                lectureMinute: %d
                studentMessage: %s
                """.formatted(language, topic, phase, minute, studentMessage);

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("input", prompt);
            body.put("max_output_tokens", 700);

            String raw = client.post().uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body).retrieve().body(String.class);
            String text = extractOutputText(raw);
            if (text == null || text.isBlank()) return Optional.empty();
            JsonNode node = mapper.readTree(stripCodeFence(text));

            return Optional.of(new AiTeacherTurnResponse(
                    value(node, "phase", phase),
                    value(node, "teacherText", "Let's continue the lesson."),
                    value(node, "teachingMode", "ADAPTIVE"),
                    value(node, "visualMode", "DIGITAL_BOARD"),
                    value(node, "nextPhase", "CHECK"),
                    node.path("askStudent").asBoolean(true),
                    value(node, "studentPrompt", ""),
                    node.path("lectureComplete").asBoolean(false)));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String extractOutputText(String raw) throws Exception {
        JsonNode root = mapper.readTree(raw);
        JsonNode direct = root.get("output_text");
        if (direct != null && direct.isTextual()) return direct.asText();
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                JsonNode text = content.get("text");
                if (text != null && text.isTextual()) return text.asText();
            }
        }
        return null;
    }

    private String stripCodeFence(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private String value(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asText(fallback);
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}