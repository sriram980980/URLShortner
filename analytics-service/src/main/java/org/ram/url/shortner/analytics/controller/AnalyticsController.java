package org.ram.url.shortner.analytics.controller;

import org.ram.url.shortner.analytics.domain.ClickEvent;
import org.ram.url.shortner.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/clicks/{shortCode}")
    public ResponseEntity<List<ClickEvent>> getClicks(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getClickEvents(shortCode));
    }

    @GetMapping("/clicks/{shortCode}/count")
    public ResponseEntity<Map<String, Object>> getClickCount(@PathVariable String shortCode) {
        long count = service.getClickCount(shortCode);
        return ResponseEntity.ok(Map.of(
            "shortCode", shortCode,
            "count", count
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "analytics-service"));
    }
}
