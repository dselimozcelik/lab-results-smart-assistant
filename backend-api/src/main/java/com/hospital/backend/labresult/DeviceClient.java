package com.hospital.backend.labresult;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

// Fetches a batch from the mock device. WebClient used synchronously via block();
// the same client style is reused for the LLM call in Phase 4.
@Component
public class DeviceClient {

    private final WebClient deviceWebClient;
    private final PollingProperties props;

    public DeviceClient(WebClient deviceWebClient, PollingProperties props) {
        this.deviceWebClient = deviceWebClient;
        this.props = props;
    }

    public List<SampleBatchDto> fetchBatch() {
        // Blank scenario => normal operation (mock returns a varied random batch).
        // A set scenario forces that case, for demos.
        String scenario = props.scenario();
        boolean forced = scenario != null && !scenario.isBlank();
        return deviceWebClient.get()
                .uri(uri -> {
                    uri.path("/api/device-results/batch");
                    if (forced) {
                        uri.queryParam("scenario", scenario);
                    }
                    return uri.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<SampleBatchDto>>() {})
                .block();
    }
}
