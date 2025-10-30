package com.example.stockservice.client;


import com.example.stockservice.exception.GenericErrorResponse;
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
        try (InputStream body = response.body().asInputStream()) {
            Map<String, String> errors =
                    mapper.readValue(IOUtils.toString(body, StandardCharsets.UTF_8), Map.class);
            return GenericErrorResponse.builder()
                    .message(errors.get("error"))
                    .httpStatus(HttpStatus.valueOf(response.status()))
                    .build();
        }catch (IOException e){
            throw GenericErrorResponse.builder()
                    .httpStatus(HttpStatus.valueOf(response.status()))
                    .message(e.getMessage())
                    .build();
        }
    }
}
