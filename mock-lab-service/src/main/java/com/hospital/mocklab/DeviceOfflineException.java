package com.hospital.mocklab;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Simulates a transient device failure: the endpoint returns HTTP 503.
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class DeviceOfflineException extends RuntimeException {
    public DeviceOfflineException() {
        super("Lab device is temporarily offline");
    }
}
