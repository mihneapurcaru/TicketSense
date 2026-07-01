package com.gitlab.mihnea_purcaru1.service_ticketsense.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NlpClassifierService {

    private final RestClient restClient;

    public NlpClassifierService(@Value("${nlp.service.url:http://localhost:8000}") String nlpServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(nlpServiceUrl)
                .build();
    }

    public ClassifyResult classify(String summary, String description) {
        try {
            ClassifyRequest request = new ClassifyRequest();
            request.setSummary(summary);
            request.setDescription(description != null ? description : "");

            return restClient.post()
                    .uri("/classify")
                    .body(request)
                    .retrieve()
                    .body(ClassifyResult.class);
        } catch (Exception e) {
            log.error("NLP classification failed, defaulting to General queue", e);
            ClassifyResult fallback = new ClassifyResult();
            fallback.setQueue("General");
            fallback.setConfidence(0.0);
            return fallback;
        }
    }

    @Data
    public static class ClassifyRequest {
        private String summary;
        private String description;
    }

    @Data
    public static class ClassifyResult {
        private String queue;
        private double confidence;
    }
}
