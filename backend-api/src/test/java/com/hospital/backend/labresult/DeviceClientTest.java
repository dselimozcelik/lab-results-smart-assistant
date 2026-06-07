package com.hospital.backend.labresult;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void slowDeviceCallTimesOut() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[]")
                .setBodyDelay(1, TimeUnit.SECONDS));
        PollingProperties properties = new PollingProperties(
                server.url("/").toString(), 10_000, Duration.ofMillis(100), "");
        DeviceClient client = new DeviceClient(
                WebClient.builder().baseUrl(properties.mockBaseUrl()).build(), properties);

        assertThatThrownBy(client::fetchBatch)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Timeout");
    }
}
