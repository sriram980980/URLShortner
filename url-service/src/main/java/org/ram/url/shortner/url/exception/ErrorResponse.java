package org.ram.url.shortner.url.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    Instant timestamp,
    Map<String, String> fieldErrors
) {
    public ErrorResponse(int status, String message) {
        this(status, message, Instant.now(), null);
    }
}
