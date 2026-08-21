package org.ram.url.shortner.url.event;

import java.time.Instant;

public record ClickEvent(
    String shortCode,
    String longUrl,
    Instant timestamp,
    String userAgent,
    String ipAddress
) {}
