package com.hospital.mocklab;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/device-results")
public class DeviceResultController {

    private final DeviceResultFactory factory;

    public DeviceResultController(DeviceResultFactory factory) {
        this.factory = factory;
    }

    // Normal operation: no scenario => a varied, realistic random batch (fresh each call).
    // ?scenario=... forces one specific case for demos/tests.
    // ?seed=... makes the random batch reproducible (used by tests).
    @GetMapping("/batch")
    public ResponseEntity<List<DeviceResultDto>> batch(
            @RequestParam(required = false) String scenario,
            @RequestParam(required = false) Long seed) {

        if (scenario == null) {
            Random rnd = (seed != null) ? new Random(seed) : new Random();
            return ResponseEntity.ok(factory.randomBatch(rnd));
        }

        Scenario resolved = Scenario.fromCode(scenario)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                        "Unknown scenario: " + scenario));

        return ResponseEntity.ok(factory.batchFor(resolved));
    }
}
