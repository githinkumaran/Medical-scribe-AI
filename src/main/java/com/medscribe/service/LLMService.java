package com.medscribe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class LLMService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSoapNote(String transcript) {

        String prompt = """
                You are a medical scribe assistant. Convert the following 
                doctor-patient conversation transcript into a structured 
                SOAP note format.
                
                Use exactly this format:
                SUBJECTIVE: [what the patient says, complaints, symptoms]
                OBJECTIVE: [vitals, physical exam findings, test results]
                ASSESSMENT: [diagnosis or clinical impression]
                PLAN: [treatment plan, medications, follow-up]
                
                Transcript:
                """ + transcript + """
                
                Generate the SOAP note now:
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", groqModel);
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                groqApiUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();
        List<Map> choices = (List<Map>) responseBody.get("choices");
        Map firstChoice = choices.get(0);
        Map messageResponse = (Map) firstChoice.get("message");
        return (String) messageResponse.get("content");
    }
}