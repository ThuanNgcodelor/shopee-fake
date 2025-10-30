package com.example.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GoogleOAuth2Service {
    
    @Value("${google.client-id}")
    private String clientId;
    
    @Value("${google.client-secret}")
    private String clientSecret;
    
    @Value("${google.redirect-uri}")
    private String redirectUri;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GoogleOAuth2Service() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public GoogleUserInfo getUserInfoFromCode(String code) {
        try {
            // Exchange authorization code for access token
            String accessToken = exchangeCodeForAccessToken(code);
            
            // Get user info using access token
            return getUserInfoFromAccessToken(accessToken);
            
        } catch (Exception e) {
            log.error("Error getting user info from Google code", e);
            throw new RuntimeException("Error getting user info from Google", e);
        }
    }

    private String exchangeCodeForAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);

        try {
            ResponseEntity<String> res = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), String.class);
            JsonNode json = objectMapper.readTree(res.getBody());
            if (res.getStatusCode().is2xxSuccessful()) {
                return json.path("access_token").asText();
            }
            // Lỗi chuẩn của Google: { "error": "...", "error_description": "..." }
            String err = json.path("error").asText();
            String desc = json.path("error_description").asText();
            throw new IllegalArgumentException("Google token exchange failed: " + err + " - " + desc);
        } catch (HttpClientErrorException ex) {
            String bodyText = ex.getResponseBodyAsString();
            log.error("Token exchange failed: {}", bodyText);
            throw new IllegalArgumentException("Google token exchange failed: " + bodyText, ex);
        } catch (Exception ex) {
            log.error("Unexpected error exchanging token", ex);
            throw new IllegalStateException("Unexpected error exchanging token", ex);
        }
    }


    private GoogleUserInfo getUserInfoFromAccessToken(String accessToken) throws JsonProcessingException {
        String userInfoUrl = "https://openidconnect.googleapis.com/v1/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        JsonNode json = objectMapper.readTree(response.getBody());
        return GoogleUserInfo.builder()
                .email(json.path("email").asText())
                .name(json.path("name").asText())
                .picture(json.path("picture").asText())
                .sub(json.path("sub").asText())
                .build();
    }

    
    public static class GoogleUserInfo {
        private String email;
        private String name;
        private String picture;
        private String sub;
        
        public static GoogleUserInfoBuilder builder() {
            return new GoogleUserInfoBuilder();
        }
        
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
        public String getSub() { return sub; }
        
        public static class GoogleUserInfoBuilder {
            private String email;
            private String name;
            private String picture;
            private String sub;
            
            public GoogleUserInfoBuilder email(String email) { this.email = email; return this; }
            public GoogleUserInfoBuilder name(String name) { this.name = name; return this; }
            public GoogleUserInfoBuilder picture(String picture) { this.picture = picture; return this; }
            public GoogleUserInfoBuilder sub(String sub) { this.sub = sub; return this; }
            
            public GoogleUserInfo build() {
                GoogleUserInfo info = new GoogleUserInfo();
                info.email = this.email;
                info.name = this.name;
                info.picture = this.picture;
                info.sub = this.sub;
                return info;
            }
        }
    }
}
