package com.masterlearning.platform.modules.ai.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.modules.ai.dto.response.GroundedTutorResponse;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import com.masterlearning.platform.modules.ai.service.AdaptiveTutorContextService;
import com.masterlearning.platform.modules.ai.service.LearnerTutorContextService;
import com.masterlearning.platform.modules.ai.service.SemanticRagService;
import com.masterlearning.platform.modules.ai.service.TutorConversationMemory;
import com.masterlearning.platform.modules.ai.service.TutorPromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroundedTutorEngine {
    private static final int RETRIEVAL_LIMIT = 5;
    private static final double SEMANTIC_WEIGHT = 0.70;
    private static final double LEXICAL_WEIGHT = 0.30;

    private final RagContextRetriever lexicalRetriever;
    private final SemanticRagService semanticRag;
    private final LearnerTutorContextService learnerContextService;
    private final TutorConversationMemory conversationMemory;
    private final TutorPromptBuilder promptBuilder;
    private final AdaptiveTutorContextService adaptiveContext;
    private final ObjectMapper objectMapper;
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public GroundedTutorEngine(
            RagContextRetriever lexicalRetriever,
            SemanticRagService semanticRag,
            LearnerTutorContextService learnerContextService,
            TutorConversationMemory conversationMemory,
            TutorPromptBuilder promptBuilder,
            AdaptiveTutorContextService adaptiveContext,
            ObjectMapper objectMapper,
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-5.6-luna}") String model) {
        this.lexicalRetriever = lexicalRetriever;
        this.semanticRag = semanticRag;
        this.learnerContextService = learnerContextService;
        this.conversationMemory = conversationMemory;
        this.promptBuilder = promptBuilder;
        this.adaptiveContext = adaptiveContext;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public GroundedTutorResponse respond(UUID enrollmentId, UUID courseId, String message) {
        String question = adaptiveContext.normalizeQuestion(message);
        if (question.isBlank()) {
            return new GroundedTutorResponse(enrollmentId,
                    "Please ask a learning question so I can help.", false, false, List.of());
        }

        LearnerTutorContext learner = learnerContextService.build(enrollmentId);
        List<String> memory = conversationMemory.recent(enrollmentId);
        List<SourceChunk> chunks = retrieve(courseId, question);
        List<GroundedTutorResponse.Source> sources = chunks.stream()
                .map(c -> new GroundedTutorResponse.Source(c.lessonId(), c.lessonTitle(), round(c.relevance())))
                .toList();

        if (chunks.isEmpty()) {
            String response = "I could not find relevant material in this course. Please ask about a topic covered by the course lessons.";
            conversationMemory.remember(enrollmentId, question, response);
            return new GroundedTutorResponse(enrollmentId, response, false, false, List.of());
        }

        String context = chunks.stream()
                .map(c -> "[Lesson: " + c.lessonTitle() + "]\n" + c.content())
                .collect(Collectors.joining("\n\n"));

        if (apiKey.isBlank()) {
            String response = "I found relevant course material in: " + chunks.get(0).lessonTitle()
                    + ". The LLM provider is not configured yet, so I will not invent an answer. Configure OPENAI_API_KEY to enable grounded generation.";
            conversationMemory.remember(enrollmentId, question, response);
            return new GroundedTutorResponse(enrollmentId, response, true, false, sources);
        }

        try {
            String prompt = promptBuilder.build(question, context, learner, memory)
                    + "\n\nADAPTIVE TUTORING:\n"
                    + "Use the learner's explanation style: " + adaptiveContext.explanationStyle(learner.masteryScore()) + ".\n"
                    + "Prefer remediation when the learner is foundational, balanced teaching for developing learners, and deeper challenge for advanced learners.";
            Map<String, Object> body = Map.of("model", model, "input", prompt);
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
            conversationMemory.remember(enrollmentId, question, text);
            return new GroundedTutorResponse(enrollmentId, text, true, true, sources);
        } catch (Exception ex) {
            String response = "The grounded AI provider is temporarily unavailable. I found relevant course material, but I will not generate an unsupported answer.";
            conversationMemory.remember(enrollmentId, question, response);
            return new GroundedTutorResponse(enrollmentId, response, true, false, sources);
        }
    }

    private List<SourceChunk> retrieve(UUID courseId, String question) {
        List<SourceChunk> semantic = new ArrayList<>();
        if (!apiKey.isBlank()) {
            try {
                semantic = semanticRag.search(courseId, question, RETRIEVAL_LIMIT).stream()
                        .map(c -> new SourceChunk(c.lessonId(), c.lessonTitle(), c.content(), clamp(c.relevance()), c.content()))
                        .toList();
            } catch (RuntimeException ignored) {
                // Continue with lexical retrieval when semantic indexing/provider is unavailable.
            }
        }

        List<SourceChunk> lexical = lexicalRetriever.retrieve(courseId, question, RETRIEVAL_LIMIT).stream()
                .map(c -> new SourceChunk(c.lessonId(), c.lessonTitle(), c.content(), clamp(c.relevance()), c.content()))
                .toList();

        if (semantic.isEmpty()) return lexical;
        if (lexical.isEmpty()) return semantic;

        Map<String, RankedChunk> merged = new LinkedHashMap<>();
        for (SourceChunk chunk : semantic) {
            String key = key(chunk);
            merged.put(key, new RankedChunk(chunk, SEMANTIC_WEIGHT * chunk.relevance()));
        }
        for (SourceChunk chunk : lexical) {
            String key = key(chunk);
            RankedChunk existing = merged.get(key);
            if (existing == null) {
                merged.put(key, new RankedChunk(chunk, LEXICAL_WEIGHT * chunk.relevance()));
            } else {
                SourceChunk base = existing.chunk();
                double combined = existing.score() + LEXICAL_WEIGHT * chunk.relevance();
                merged.put(key, new RankedChunk(
                        new SourceChunk(base.lessonId(), base.lessonTitle(), base.content(), clamp(combined), base.content()),
                        combined));
            }
        }

        return merged.values().stream()
                .sorted(Comparator.comparingDouble(RankedChunk::score).reversed())
                .limit(RETRIEVAL_LIMIT)
                .map(RankedChunk::chunk)
                .toList();
    }

    private String key(SourceChunk chunk) {
        return chunk.lessonId() + ":" + Integer.toHexString(chunk.contentKey().hashCode());
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record SourceChunk(UUID lessonId, String lessonTitle, String content, double relevance, String contentKey) {}

    private record RankedChunk(SourceChunk chunk, double score) {}
}
