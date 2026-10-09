package com.medicareplus.config;

import org.springframework.context.annotation.*;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean RestClient.Builder restClientBuilder() { return RestClient.builder(); }
}
