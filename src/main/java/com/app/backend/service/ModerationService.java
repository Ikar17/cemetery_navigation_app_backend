package com.app.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ModerationService {

    private final RestTemplate restTemplate;

    @Autowired
    public ModerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean checkTextContent(String text) {
        String url = "http://localhost:5000/check-text";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = "{\"text\":\"" + text + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody().contains("\"isAllowed\":true");
    }
}
