package org.ram.url.shortner.url.service;

import org.ram.url.shortner.url.domain.UrlEntry;
import org.ram.url.shortner.url.dto.ShortenRequest;
import org.ram.url.shortner.url.dto.ShortenResponse;
import org.ram.url.shortner.url.event.ClickEvent;
import org.ram.url.shortner.url.event.ClickEventProducer;
import org.ram.url.shortner.url.exception.UrlNotFoundException;
import org.ram.url.shortner.url.repository.UrlRepository;
import org.ram.url.shortner.url.util.Base62;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UrlShortenerService {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private final UrlRepository repository;
    private final ClickEventProducer clickEventProducer;
    private final Executor clickEventExecutor;

    public UrlShortenerService(
            UrlRepository repository,
            ClickEventProducer clickEventProducer,
            @Qualifier("clickEventExecutor") Executor clickEventExecutor) {
        this.repository = repository;
        this.clickEventProducer = clickEventProducer;
        this.clickEventExecutor = clickEventExecutor;
    }

    public ShortenResponse createShortUrl(ShortenRequest request, String baseUrl) {
        Instant expiresAt = (request.customTtlSeconds() != null && request.customTtlSeconds() > 0)
            ? Instant.now().plusSeconds(request.customTtlSeconds())
            : null;

        long seedId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(100, 999);
        String shortCode = Base62.encode(seedId);

        UrlEntry entry = new UrlEntry(shortCode, request.longUrl(), Instant.now(), expiresAt);
        repository.save(entry);

        log.info("Created short URL: shortCode={} for longUrl={}", shortCode, request.longUrl());
        return new ShortenResponse(shortCode, baseUrl + "/" + shortCode, request.longUrl(), expiresAt);
    }

    public String resolveUrl(String shortCode, String userAgent, String ipAddress) {
        UrlEntry entry = repository.findByShortCode(shortCode)
            .filter(e -> !e.isExpired())
            .orElseThrow(() -> new UrlNotFoundException(shortCode));

        String longUrl = entry.getLongUrl();
        Instant clickedAt = Instant.now();

        CompletableFuture.runAsync(() -> {
            try {
                repository.incrementClickCount(shortCode);
                clickEventProducer.publishClickEvent(
                    new ClickEvent(shortCode, longUrl, clickedAt, userAgent, ipAddress)
                );
            } catch (Exception ex) {
                log.error("Async click processing failed for shortCode={}: {}", shortCode, ex.getMessage());
            }
        }, clickEventExecutor);

        return longUrl;
    }

    public UrlEntry getUrlStats(String shortCode) {
        return repository.findByShortCodeWithStats(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }
}
