package com.hospital.backend.labresult;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient deviceWebClient(PollingProperties props) {
        return WebClient.builder()
                .baseUrl(props.mockBaseUrl())
                .build();
    }
}
