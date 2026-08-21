package org.ram.url.shortner.analytics.dto;

import java.time.Instant;

public record ClickEventMessage(
    String shortCode,
    String longUrl,
    Instant timestamp,
    String userAgent,
    String ipAddress
) {}
