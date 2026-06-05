package com.hospital.mocklab;

import java.util.Arrays;
import java.util.Optional;

// Controllable batch scenarios the device can emit. The query param maps to these.
public enum Scenario {
    NORMAL("normal"),
    ABNORMAL("abnormal"),
    CRITICAL("critical"),
    DUPLICATE("duplicate"),
    MISSING_FIELD("missing-field"),
    INVALID_UNIT("invalid-unit"),
    STALE("stale"),
    DEVICE_ERROR("device-error");

    private final String code;

    Scenario(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<Scenario> fromCode(String code) {
        return Arrays.stream(values()).filter(s -> s.code.equals(code)).findFirst();
    }
}
