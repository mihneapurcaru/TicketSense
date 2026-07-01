package com.gitlab.mihnea_purcaru1.service_ticketsense.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class AiSummarizationService {

    private static final String MODEL = "llama3.2";
    private static final String PROMPT_TEMPLATE = """
            You are an IT support assistant. Summarize the following support ticket in 2-3 concise sentences.
            Focus on: what the problem is, what is affected, and any relevant context.
            Do not include greetings, conclusions, or suggestions — just the summary.

            Ticket title: %s

            Ticket description:
            %s
            """;

    private final RestClient restClient;

    public AiSummarizationService(
            @Value("${ollama.service.url:http://localhost:11434}") String ollamaUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(ollamaUrl)
                .build();
    }

    public String summarize(String title, String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        OllamaRequest request = new OllamaRequest();
        request.setModel(MODEL);
        request.setPrompt(PROMPT_TEMPLATE.formatted(title, description));
        request.setStream(false);

        try {
            OllamaResponse response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response != null && response.getResponse() != null) {
                return response.getResponse().trim();
            }
        } catch (Exception e) {
            log.error("AI summarization failed for ticket '{}'", title, e);
        }

        return null;
    }

    @Data
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
    }

    @Data
    public static class OllamaResponse {
        private String response;
        private boolean done;
    }
}
