package com.example.authservice.config;

import com.example.authservice.client.CustomErrorDecoder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // Add internal call header for user-service requests
            if (template.url().contains("/v1/user/")) {
                template.header("X-Internal-Call", "true");
            }
        };
    }
}
