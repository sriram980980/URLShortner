package org.ram.url.shortner.url.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
    @NotBlank(message = "URL cannot be empty")
    @URL(message = "Invalid URL format")
    String longUrl,
    Long customTtlSeconds
) {}
