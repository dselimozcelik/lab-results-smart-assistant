package com.hospital.backend.common;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unknownPathMapsToNotFoundNotServerError() {
        NoResourceFoundException ex =
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "api/lab-results");

        ProblemDetail pd = handler.handleNoResource(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void missingEntityMapsToNotFound() {
        ProblemDetail pd = handler.handleNotFound(new EntityNotFoundException("Sample not found: S-1"));

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getDetail()).contains("S-1");
    }

    @Test
    void invalidQueryParameterMapsToBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "NOPE", com.hospital.backend.labresult.AnomalyStatus.class,
                "status", null, new IllegalArgumentException("bad enum"));

        ProblemDetail pd = handler.handleTypeMismatch(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getDetail()).contains("status");
    }
}
