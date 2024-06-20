package com.app.backend.service;

import com.app.backend.config.MultipartInputStreamFileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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

    public boolean checkImageContent(MultipartFile image) throws IOException {
        String url = "http://localhost:5000/check-image";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new MultipartInputStreamFileResource(image.getInputStream(), image.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Boolean> bodyMap = response.getBody();
            return bodyMap != null && bodyMap.get("isAllowed");
        }

        return false;
    }
}