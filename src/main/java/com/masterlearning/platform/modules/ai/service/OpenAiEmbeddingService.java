package com.masterlearning.platform.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OpenAiEmbeddingService {
    private static final int VECTOR_DIMENSIONS = 1536;

    private final ObjectMapper objectMapper;
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public OpenAiEmbeddingService(
            ObjectMapper objectMapper,
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_EMBEDDING_MODEL:text-embedding-3-small}") String model) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public List<Double> embed(String text) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not configured");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding input must not be blank");
        }
        try {
            String response = client.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(java.util.Map.of("model", model, "input", text.trim()))
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode vector = root.path("data").path(0).path("embedding");
            if (!vector.isArray() || vector.isEmpty()) {
                throw new IllegalStateException("Embedding response was empty");
            }
            if (vector.size() != VECTOR_DIMENSIONS) {
                throw new IllegalStateException(
                        "Embedding dimension " + vector.size() + " does not match pgvector dimension " + VECTOR_DIMENSIONS);
            }
            java.util.ArrayList<Double> result = new java.util.ArrayList<>(vector.size());
            vector.forEach(node -> result.add(node.asDouble()));
            return result;
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException) throw (IllegalStateException) ex;
            throw new IllegalStateException("Unable to create embedding", ex);
        }
    }
}
