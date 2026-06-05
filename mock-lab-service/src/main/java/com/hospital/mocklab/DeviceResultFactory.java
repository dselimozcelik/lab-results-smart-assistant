package com.hospital.mocklab;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

// Builds device batches. Two modes:
//  - batchFor(scenario): forces one specific case, deterministic, for demos/tests.
//  - randomBatch(): the "normal operation" stream — a varied, plausible batch with a
//    realistic mix (mostly normal, some abnormal, rare critical, occasional bad data),
//    fresh sampleIds and measuredAt=now, just like a real analyser.
@Component
public class DeviceResultFactory {

    private static final Instant REF = Instant.parse("2026-06-05T12:00:00Z");

    // Monotonic counter guarantees unique sampleIds across the whole run (no accidental dupes).
    private final AtomicLong seq = new AtomicLong(1);

    public List<DeviceResultDto> batchFor(Scenario scenario) {
        return switch (scenario) {
            case NORMAL -> List.of(
                    glucose("S-1001", "P-100", 95.0, REF),
                    potassium("S-1002", "P-101", 4.2, REF)
            );
            case ABNORMAL -> List.of(
                    glucose("S-2001", "P-200", 55.0, REF),
                    potassium("S-2002", "P-201", 6.1, REF)
            );
            case CRITICAL -> List.of(
                    glucose("S-3001", "P-300", 350.0, REF),
                    potassium("S-3002", "P-301", 1.5, REF)
            );
            case DUPLICATE -> List.of(
                    glucose("S-4001", "P-400", 90.0, REF),
                    glucose("S-4001", "P-400", 90.0, REF)
            );
            case MISSING_FIELD -> List.of(
                    new DeviceResultDto("S-5001", "P-500", "GLU", "Glucose",
                            null, "mg/dL", 70.0, 110.0, REF, "DEV-1")
            );
            case INVALID_UNIT -> List.of(
                    new DeviceResultDto("S-6001", "P-600", "GLU", "Glucose",
                            95.0, "banana", 70.0, 110.0, REF, "DEV-1")
            );
            case STALE -> List.of(
                    glucose("S-7001", "P-700", 95.0, REF.minus(400, ChronoUnit.DAYS))
            );
            case DEVICE_ERROR -> throw new DeviceOfflineException();
        };
    }

    // Normal-operation stream. Pass a fresh Random (seeded for tests, unseeded for live demo).
    public List<DeviceResultDto> randomBatch(Random rnd) {
        int size = 3 + rnd.nextInt(6); // 3..8 records
        List<DeviceResultDto> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            batch.add(randomRecord(rnd));
        }
        return batch;
    }

    private DeviceResultDto randomRecord(Random rnd) {
        TestType t = TestType.ALL.get(rnd.nextInt(TestType.ALL.size()));
        String sampleId = "S-" + seq.getAndIncrement();
        String patientId = "P-" + (100 + rnd.nextInt(900));
        Instant now = Instant.now();

        int roll = rnd.nextInt(100);
        if (roll < 70) {
            // normal: value inside reference band
            return record(t, sampleId, patientId, within(rnd, t.refMin, t.refMax), t.unit, now);
        } else if (roll < 90) {
            // abnormal: just outside one bound
            double range = t.refMax - t.refMin;
            double value = rnd.nextBoolean()
                    ? t.refMin - within(rnd, 0.05 * range, 0.4 * range)
                    : t.refMax + within(rnd, 0.05 * range, 0.4 * range);
            return record(t, sampleId, patientId, value, t.unit, now);
        } else if (roll < 95) {
            // critical: far beyond a bound
            double range = t.refMax - t.refMin;
            double value = rnd.nextBoolean()
                    ? t.refMin - within(rnd, 1.0 * range, 2.0 * range)
                    : t.refMax + within(rnd, 1.0 * range, 2.0 * range);
            return record(t, sampleId, patientId, value, t.unit, now);
        } else {
            // ~5% bad data, split across the three invalid kinds
            return badRecord(rnd, t, sampleId, patientId, now);
        }
    }

    private DeviceResultDto badRecord(Random rnd, TestType t, String sampleId, String patientId, Instant now) {
        return switch (rnd.nextInt(3)) {
            case 0 -> // missing value
                    new DeviceResultDto(sampleId, patientId, t.code, t.name,
                            null, t.unit, t.refMin, t.refMax, now, t.deviceId);
            case 1 -> // invalid unit
                    new DeviceResultDto(sampleId, patientId, t.code, t.name,
                            within(rnd, t.refMin, t.refMax), "??", t.refMin, t.refMax, now, t.deviceId);
            default -> // stale measurement
                    record(t, sampleId, patientId, within(rnd, t.refMin, t.refMax), t.unit,
                            now.minus(400, ChronoUnit.DAYS));
        };
    }

    private DeviceResultDto record(TestType t, String sampleId, String patientId,
                                   double value, String unit, Instant measuredAt) {
        return new DeviceResultDto(sampleId, patientId, t.code, t.name,
                round(value), unit, t.refMin, t.refMax, measuredAt, t.deviceId);
    }

    private double within(Random rnd, double min, double max) {
        return min + rnd.nextDouble() * (max - min);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private DeviceResultDto glucose(String sampleId, String patientId, double value, Instant measuredAt) {
        return new DeviceResultDto(sampleId, patientId, "GLU", "Glucose",
                value, "mg/dL", 70.0, 110.0, measuredAt, "DEV-1");
    }

    private DeviceResultDto potassium(String sampleId, String patientId, double value, Instant measuredAt) {
        return new DeviceResultDto(sampleId, patientId, "K", "Potassium",
                value, "mmol/L", 3.5, 5.1, measuredAt, "DEV-2");
    }
}
