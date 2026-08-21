package org.ram.url.shortner.url.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortCode) {
        super("URL not found or expired: " + shortCode);
    }
}
