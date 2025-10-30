package com.example.orderservice.client;


import com.example.orderservice.exception.GenericErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String s, Response response) {
        try {

            if (response.status() >= 200 && response.status() < 300) {
                return null;
            }
            if (response.status() == 404) {
                return new RuntimeException("Resource not found");
            }
            if (response.status() == 400) {
                return new RuntimeException("Bad request");
            }
            if (response.status() == 401) {
                return new RuntimeException("Unauthorized access");
            }
            if (response.status() == 403) {
                return new RuntimeException("Forbidden access");
            }
            if (response.status() == 500) {
                return new RuntimeException("Internal server error");
            }
            if (response.status() == 503) {
                return new RuntimeException("Service unavailable");
            }
            if (response.body() == null) {
                return GenericErrorResponse.builder()
                        .httpStatus(HttpStatus.valueOf(response.status()))
                        .message("No response body from remote service")
                        .build();
            }

            try (InputStream body = response.body().asInputStream()) {
                Map<String, String> errors =
                        mapper.readValue(IOUtils.toString(body, StandardCharsets.UTF_8), Map.class);

                return GenericErrorResponse.builder()
                        .message(errors.getOrDefault("error", "Unknown error"))
                        .httpStatus(HttpStatus.valueOf(response.status()))
                        .build();
            }
        } catch (IOException e) {
            return GenericErrorResponse.builder()
                    .httpStatus(HttpStatus.valueOf(response.status()))
                    .message("Failed to decode error: " + e.getMessage())
                    .build();
        }
    }

}

