package com.study.backend.aistudy.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class GeminiEmbeddingModel implements EmbeddingModel {

    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/openai/embeddings";

    private static final String EMBEDDING_MODEL = "gemini-embedding-001";
    private static final int DIMENSIONS = 768;

    private final RestClient restClient;
    private final String apiKey;

    public GeminiEmbeddingModel(@Value("${spring.ai.openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getText());
    }

    @Override
    @SuppressWarnings("unchecked")
    public EmbeddingResponse call(EmbeddingRequest request) {
        Map<String, Object> body = Map.of(
                "model", EMBEDDING_MODEL,
                "input", request.getInstructions(),
                "dimensions", DIMENSIONS
        );

        Map<?, ?> response = restClient.post()
                .uri(EMBEDDING_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<?> data = (List<?>) response.get("data");
        List<Embedding> result = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            Map<?, ?> item = (Map<?, ?>) data.get(i);
            List<Double> values = (List<Double>) item.get("embedding");

            result.add(new Embedding(toFloatArray(values), i));
        }

        return new EmbeddingResponse(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        Map<String, Object> body = Map.of(
                "model", EMBEDDING_MODEL,
                "input", text,
                "dimensions", DIMENSIONS
        );

        Map<?, ?> response = restClient.post()
                .uri(EMBEDDING_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<?> data = (List<?>) response.get("data");
        Map<?, ?> first = (Map<?, ?>) data.get(0);
        List<Double> values = (List<Double>) first.get("embedding");

        return toFloatArray(values);
    }

    private float[] toFloatArray(List<Double> values) {
        float[] result = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).floatValue();
        }

        return result;
    }
}