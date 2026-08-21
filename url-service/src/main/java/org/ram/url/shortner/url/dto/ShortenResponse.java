package org.ram.url.shortner.url.dto;

import java.time.Instant;

public record ShortenResponse(
    String shortCode,
    String shortUrl,
    String originalUrl,
    Instant expiresAt
) {}
