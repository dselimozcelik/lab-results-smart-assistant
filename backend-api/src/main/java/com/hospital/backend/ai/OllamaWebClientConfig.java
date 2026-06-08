package com.hospital.backend.ai;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class OllamaWebClientConfig {

    // The blocking call in OllamaClient caps the WAIT for a response, but not the earlier phases.
    // Configure the transport too: a short connect timeout (TCP must establish quickly or Ollama is
    // down) and a response timeout matching the configured budget (so a model that accepts the
    // connection but never replies still releases the thread). Without these, a half-open socket or
    // a hung model could block past the intended timeout.
    @Bean
    public WebClient ollamaWebClient(OllamaProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(props.timeout());

        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
