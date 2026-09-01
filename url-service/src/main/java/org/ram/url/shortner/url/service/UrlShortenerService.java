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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        // Requirement 4: Ensure idempotency - look up originalUrl via unique index first
        Optional<UrlEntry> existingEntry = repository.findByOriginalUrl(request.longUrl());
        if (existingEntry.isPresent()) {
            UrlEntry entry = existingEntry.get();
            if (!entry.isExpired()) {
                log.info("Idempotent hit: returning existing short URL shortCode={} for longUrl={}",
                        entry.getShortCode(), request.longUrl());
                return new ShortenResponse(
                        entry.getShortCode(),
                        baseUrl + "/" + entry.getShortCode(),
                        entry.getOriginalUrl(),
                        entry.getExpiresAt()
                );
            }
        }

        Instant expiresAt = (request.customTtlSeconds() != null && request.customTtlSeconds() > 0)
                ? Instant.now().plusSeconds(request.customTtlSeconds())
                : null;

        // Requirement 2 & 3: Atomic sequence ID + bit-permutation obfuscated 8-char Base62 string
        long nextId = repository.nextId();
        String shortCode = Base62.encodeId(nextId);

        UrlEntry newEntry = new UrlEntry(shortCode, request.longUrl(), Instant.now(), expiresAt);

        // Requirement 5: Handle concurrent race conditions cleanly with DuplicateKeyException catch blocks
        try {
            repository.save(newEntry);
            log.info("Created short URL: shortCode={} for longUrl={}", shortCode, request.longUrl());
            return new ShortenResponse(shortCode, baseUrl + "/" + shortCode, request.longUrl(), expiresAt);
        } catch (DuplicateKeyException ex) {
            log.warn("Concurrent duplicate insertion race condition caught for longUrl={}. Fetching existing winner entry.",
                    request.longUrl());
            return repository.findByOriginalUrl(request.longUrl())
                    .map(winner -> new ShortenResponse(
                            winner.getShortCode(),
                            baseUrl + "/" + winner.getShortCode(),
                            winner.getOriginalUrl(),
                            winner.getExpiresAt()
                    ))
                    .orElseThrow(() -> ex);
        }
    }

    public String resolveUrl(String shortCode, String userAgent, String ipAddress) {
        UrlEntry entry = repository.findByShortCode(shortCode)
                .filter(e -> !e.isExpired())
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        String originalUrl = entry.getOriginalUrl();
        Instant clickedAt = Instant.now();

        CompletableFuture.runAsync(() -> {
            try {
                repository.incrementClickCount(shortCode);
                clickEventProducer.publishClickEvent(
                        new ClickEvent(shortCode, originalUrl, clickedAt, userAgent, ipAddress)
                );
            } catch (Exception ex) {
                log.error("Async click processing failed for shortCode={}: {}", shortCode, ex.getMessage());
            }
        }, clickEventExecutor);

        return originalUrl;
    }

    public UrlEntry getUrlStats(String shortCode) {
        return repository.findByShortCodeWithStats(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }
}
