package com.hospital.backend.patient;

import java.time.Instant;

// One numeric measurement of a single test over time, used to draw a trend sparkline.
public record TestHistoryPoint(Instant measuredAt, Double value) {
}
