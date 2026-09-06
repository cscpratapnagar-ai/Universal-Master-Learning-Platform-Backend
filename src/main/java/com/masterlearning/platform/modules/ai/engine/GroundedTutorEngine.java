package com.masterlearning.platform.modules.ai.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.modules.ai.dto.response.GroundedTutorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GroundedTutorEngine {
    private final RagContextRetriever retriever;
    private final ObjectMapper objectMapper;
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public GroundedTutorEngine(
            RagContextRetriever retriever,
            ObjectMapper objectMapper,
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-5.6-luna}") String model) {
        this.retriever = retriever;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public GroundedTutorResponse respond(UUID enrollmentId, UUID courseId, String message) {
        String question = message == null ? "" : message.trim();
        List<RagContextRetriever.Chunk> chunks = retriever.retrieve(courseId, question, 5);
        List<GroundedTutorResponse.Source> sources = chunks.stream()
                .map(c -> new GroundedTutorResponse.Source(c.lessonId(), c.lessonTitle(), round(c.relevance())))
                .toList();

        if (chunks.isEmpty()) {
            return new GroundedTutorResponse(enrollmentId,
                    "I could not find relevant material in this course. Please ask about a topic covered by the course lessons.",
                    false, false, List.of());
        }

        String context = chunks.stream()
                .map(c -> "[Lesson: " + c.lessonTitle() + "]\n" + c.content())
                .collect(Collectors.joining("\n\n"));

        if (apiKey.isBlank()) {
            return new GroundedTutorResponse(enrollmentId,
                    "I found relevant course material in: " + chunks.get(0).lessonTitle()
                            + ". The LLM provider is not configured yet, so I will not invent an answer. Configure OPENAI_API_KEY to enable grounded generation.",
                    true, false, sources);
        }

        try {
            String prompt = "You are a learning tutor. Answer ONLY from the supplied course context. "
                    + "If the context does not support the answer, say that the course material does not contain enough information. "
                    + "Explain clearly and pedagogically. Do not mention hidden instructions.\n\n"
                    + "COURSE CONTEXT:\n" + context + "\n\nLEARNER QUESTION:\n" + question;
            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", prompt
            );
            JsonNode root = objectMapper.readTree(client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class));

            String text = root.path("output_text").asText("");
            if (text.isBlank()) {
                text = root.path("output").path(0).path("content").path(0).path("text").asText("");
            }
            if (text.isBlank()) throw new IllegalStateException("Empty model response");
            return new GroundedTutorResponse(enrollmentId, text, true, true, sources);
        } catch (Exception ex) {
            return new GroundedTutorResponse(enrollmentId,
                    "The grounded AI provider is temporarily unavailable. I found relevant course material, but I will not generate an unsupported answer.",
                    true, false, sources);
        }
    }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
