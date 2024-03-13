package com.ondra.knowledgebasebe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${gotenberg.port}")
    private String gotenbergPort;

    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl("http://localhost:" + gotenbergPort).build();
    }

}
