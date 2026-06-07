package com.hospital.mocklab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceResultController.class)
@Import(DeviceResultFactory.class)
class DeviceResultControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void forcedScenarioReturnsADeviceBatch() throws Exception {
        mockMvc.perform(get("/api/device-results/batch?scenario=critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sampleId").isNotEmpty())
                .andExpect(jsonPath("$[0].tests").isArray());
    }

    @Test
    void unknownScenarioReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/device-results/batch?scenario=nope"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deviceErrorScenarioReturnsServiceUnavailable() throws Exception {
        mockMvc.perform(get("/api/device-results/batch?scenario=device-error"))
                .andExpect(status().isServiceUnavailable());
    }
}
