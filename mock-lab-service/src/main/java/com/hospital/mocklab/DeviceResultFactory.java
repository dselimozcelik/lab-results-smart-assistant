package com.hospital.mocklab;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

// Builds device batches of TUBES. A real analyser processes a tube and reports a PANEL: one
// patient, one sampleId, one measuredAt, several tests. Two modes:
//  - batchFor(scenario): forces one specific case, deterministic, for demos/tests.
//  - randomBatch(): the "normal operation" stream — a few tubes, each a realistic panel with a
//    plausible mix (mostly normal, some abnormal, rare critical, occasional bad data).
@Component
public class DeviceResultFactory {

    private static final Instant REF = Instant.parse("2026-06-05T12:00:00Z");

    // A boot-specific prefix plus a counter prevents accidental duplicates after service restarts.
    private final String runId;
    private final AtomicLong seq = new AtomicLong(1);

    public DeviceResultFactory() {
        this(UUID.randomUUID().toString().substring(0, 8));
    }

    DeviceResultFactory(String runId) {
        this.runId = runId;
    }

    public List<SampleBatchDto> batchFor(Scenario scenario) {
        return switch (scenario) {
            case NORMAL -> List.of(
                    tube("S-1001", "P-100", REF, "DEV-1",
                            test(TestType.GLUCOSE, 95.0), test(TestType.POTASSIUM, 4.2)));
            case ABNORMAL -> List.of(
                    tube("S-2001", "P-200", REF, "DEV-1",
                            test(TestType.GLUCOSE, 55.0), test(TestType.POTASSIUM, 6.1)));
            case CRITICAL -> List.of(
                    tube("S-3001", "P-300", REF, "DEV-1",
                            test(TestType.GLUCOSE, 350.0), test(TestType.POTASSIUM, 1.5)));
            case DUPLICATE -> List.of(
                    tube("S-4001", "P-400", REF, "DEV-1", test(TestType.GLUCOSE, 90.0)),
                    tube("S-4001", "P-400", REF, "DEV-1", test(TestType.GLUCOSE, 90.0)));
            case MISSING_FIELD -> List.of(
                    tube("S-5001", "P-500", REF, "DEV-1",
                            new TestResultDto("GLU", "Glucose", "mg/dL", null, 70.0, 110.0)));
            case INVALID_UNIT -> List.of(
                    tube("S-6001", "P-600", REF, "DEV-1",
                            new TestResultDto("GLU", "Glucose", "banana", 95.0, 70.0, 110.0)));
            case STALE -> List.of(
                    tube("S-7001", "P-700", REF.minus(400, ChronoUnit.DAYS), "DEV-1",
                            test(TestType.GLUCOSE, 95.0)));
            case DEVICE_ERROR -> throw new DeviceOfflineException();
        };
    }

    // Normal-operation stream. Pass a fresh Random (seeded for tests, unseeded for live demo).
    public List<SampleBatchDto> randomBatch(Random rnd) {
        int tubeCount = 2 + rnd.nextInt(3); // 2..4 tubes
        List<SampleBatchDto> batch = new ArrayList<>(tubeCount);
        for (int i = 0; i < tubeCount; i++) {
            batch.add(randomTube(rnd));
        }
        return batch;
    }

    private SampleBatchDto randomTube(Random rnd) {
        String sampleId = "S-" + runId + "-" + seq.getAndIncrement();
        String patientId = "P-" + (100 + rnd.nextInt(900));
        Instant now = Instant.now();

        // A panel of 3..6 distinct tests for this one patient/tube.
        List<TestType> catalogue = new ArrayList<>(TestType.ALL);
        Collections.shuffle(catalogue, rnd);
        int panelSize = 3 + rnd.nextInt(Math.min(4, catalogue.size() - 2));
        String deviceId = catalogue.get(0).deviceId;

        List<TestResultDto> tests = new ArrayList<>(panelSize);
        for (int i = 0; i < panelSize; i++) {
            tests.add(randomTest(rnd, catalogue.get(i)));
        }
        return new SampleBatchDto(sampleId, patientId, now, deviceId, tests);
    }

    private TestResultDto randomTest(Random rnd, TestType t) {
        int roll = rnd.nextInt(100);
        if (roll < 70) {
            return test(t, within(rnd, t.refMin, t.refMax)); // normal
        } else if (roll < 90) {
            double range = t.refMax - t.refMin;
            double value = rnd.nextBoolean()
                    ? t.refMin - within(rnd, 0.05 * range, 0.4 * range)
                    : t.refMax + within(rnd, 0.05 * range, 0.4 * range);
            return test(t, value); // abnormal: just outside one bound
        } else if (roll < 95) {
            double range = t.refMax - t.refMin;
            double value = rnd.nextBoolean()
                    ? t.refMin - within(rnd, 1.0 * range, 2.0 * range)
                    : t.refMax + within(rnd, 1.0 * range, 2.0 * range);
            return test(t, value); // critical: far beyond a bound
        } else {
            return badTest(rnd, t); // ~5% bad data
        }
    }

    private TestResultDto badTest(Random rnd, TestType t) {
        return switch (rnd.nextInt(2)) {
            case 0 -> // missing value
                    new TestResultDto(t.code, t.name, t.unit, null, t.refMin, t.refMax);
            default -> // invalid unit
                    new TestResultDto(t.code, t.name, "??", within(rnd, t.refMin, t.refMax),
                            t.refMin, t.refMax);
        };
    }

    private SampleBatchDto tube(String sampleId, String patientId, Instant measuredAt,
                                String deviceId, TestResultDto... tests) {
        return new SampleBatchDto(sampleId, patientId, measuredAt, deviceId, List.of(tests));
    }

    private TestResultDto test(TestType t, double value) {
        return new TestResultDto(t.code, t.name, t.unit, round(value), t.refMin, t.refMax);
    }

    private double within(Random rnd, double min, double max) {
        return min + rnd.nextDouble() * (max - min);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
