package org.ram.url.shortner.url.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.ram.url.shortner.url.domain.UrlEntry;
import org.ram.url.shortner.url.dto.ShortenRequest;
import org.ram.url.shortner.url.dto.ShortenResponse;
import org.ram.url.shortner.url.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {
    private final UrlShortenerService service;
    private final String baseUrl;

    public UrlController(UrlShortenerService service,
                         @Value("${url-service.base-url:http://localhost:8080}") String baseUrl) {
        this.service = service;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShortUrl(request, baseUrl));
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{6,10}}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest servletRequest) {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ip = getClientIp(servletRequest);
        String longUrl = service.resolveUrl(shortCode, userAgent, ip);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }

    @GetMapping("/api/v1/urls/{shortCode}/stats")
    public ResponseEntity<UrlEntry> stats(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getUrlStats(shortCode));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
