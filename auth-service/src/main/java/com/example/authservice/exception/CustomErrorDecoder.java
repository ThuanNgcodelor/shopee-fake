package com.example.authservice.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        if (response.body() == null) {
            return new Exception("No response body");
        }
        return new ResponseStatusException(
            HttpStatus.valueOf(response.status()),
            "Error occurred while calling " + methodKey
        );
    }
} 