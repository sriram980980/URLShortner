package org.ram.url.shortner.url.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ram.url.shortner.url.domain.UrlEntry;
import org.ram.url.shortner.url.dto.ShortenRequest;
import org.ram.url.shortner.url.dto.ShortenResponse;
import org.ram.url.shortner.url.event.ClickEventProducer;
import org.ram.url.shortner.url.exception.UrlNotFoundException;
import org.ram.url.shortner.url.repository.UrlRepository;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private ClickEventProducer clickEventProducer;

    @Mock
    private java.util.concurrent.Executor clickEventExecutor;

    @InjectMocks
    private UrlShortenerService service;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8081";
        lenient().doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(clickEventExecutor).execute(any(Runnable.class));
    }

    @Test
    void createShortUrl_newUrl_success() {
        // Arrange
        String longUrl = "https://www.example.com/very/long/url/path";
        ShortenRequest request = new ShortenRequest(longUrl, 3600L);

        when(repository.findByOriginalUrl(longUrl)).thenReturn(Optional.empty());
        when(repository.nextId()).thenReturn(42L);

        // Act
        ShortenResponse response = service.createShortUrl(request, baseUrl);

        // Assert
        assertNotNull(response.shortCode());
        assertEquals(8, response.shortCode().length(), "Short code must be exactly 8 characters long");
        assertTrue(response.shortUrl().startsWith(baseUrl + "/"));
        assertEquals(longUrl, response.originalUrl());
        assertNotNull(response.expiresAt());

        verify(repository, times(1)).findByOriginalUrl(longUrl);
        verify(repository, times(1)).nextId();
        verify(repository, times(1)).save(any(UrlEntry.class));
    }

    @Test
    void createShortUrl_idempotent_existingUrl() {
        // Arrange
        String longUrl = "https://www.example.com/existing";
        ShortenRequest request = new ShortenRequest(longUrl, 3600L);
        String existingShortCode = "AbCd1234";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        UrlEntry existingEntry = new UrlEntry(existingShortCode, longUrl, Instant.now(), expiresAt);
        when(repository.findByOriginalUrl(longUrl)).thenReturn(Optional.of(existingEntry));

        // Act
        ShortenResponse response = service.createShortUrl(request, baseUrl);

        // Assert
        assertEquals(existingShortCode, response.shortCode());
        assertEquals(baseUrl + "/" + existingShortCode, response.shortUrl());
        assertEquals(longUrl, response.originalUrl());
        assertEquals(expiresAt, response.expiresAt());

        verify(repository, times(1)).findByOriginalUrl(longUrl);
        verify(repository, never()).nextId();
        verify(repository, never()).save(any(UrlEntry.class));
    }

    @Test
    void createShortUrl_concurrentRaceCondition_duplicateKeyException() {
        // Arrange
        String longUrl = "https://www.example.com/race-condition";
        ShortenRequest request = new ShortenRequest(longUrl, 3600L);
        String winnerShortCode = "W1nN3r88";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        UrlEntry winnerEntry = new UrlEntry(winnerShortCode, longUrl, Instant.now(), expiresAt);

        // First lookup misses (before race)
        when(repository.findByOriginalUrl(longUrl))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winnerEntry)); // Second lookup after race condition finds winner

        when(repository.nextId()).thenReturn(100L);
        when(repository.save(any(UrlEntry.class))).thenThrow(new DuplicateKeyException("E11000 duplicate key error"));

        // Act
        ShortenResponse response = service.createShortUrl(request, baseUrl);

        // Assert
        assertNotNull(response);
        assertEquals(winnerShortCode, response.shortCode());
        assertEquals(baseUrl + "/" + winnerShortCode, response.shortUrl());
        assertEquals(longUrl, response.originalUrl());

        verify(repository, times(2)).findByOriginalUrl(longUrl);
        verify(repository, times(1)).nextId();
        verify(repository, times(1)).save(any(UrlEntry.class));
    }

    @Test
    void resolveUrl_success() {
        // Arrange
        String shortCode = "abc12345";
        String longUrl = "https://www.example.com/target";
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);

        UrlEntry urlEntry = new UrlEntry(shortCode, longUrl, now, expiresAt);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlEntry));

        // Act
        String result = service.resolveUrl(shortCode, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertEquals(longUrl, result);
        verify(repository, times(1)).findByShortCode(shortCode);
        verify(repository, times(1)).incrementClickCount(shortCode);
        verify(clickEventProducer, times(1)).publishClickEvent(any());
    }

    @Test
    void resolveUrl_notFound() {
        // Arrange
        String shortCode = "notexist";
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UrlNotFoundException.class, () -> {
            service.resolveUrl(shortCode, "Mozilla/5.0", "192.168.1.1");
        });
        verify(repository, times(1)).findByShortCode(shortCode);
        verify(clickEventProducer, never()).publishClickEvent(any());
    }

    @Test
    void resolveUrl_expired() {
        // Arrange
        String shortCode = "expired1";
        String longUrl = "https://www.example.com/target";
        Instant now = Instant.now();
        Instant expiresAt = now.minusSeconds(3600); // expired in the past

        UrlEntry urlEntry = new UrlEntry(shortCode, longUrl, now.minusSeconds(7200), expiresAt);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlEntry));

        // Act & Assert
        assertThrows(UrlNotFoundException.class, () -> {
            service.resolveUrl(shortCode, "Mozilla/5.0", "192.168.1.1");
        });
        verify(repository, times(1)).findByShortCode(shortCode);
        verify(clickEventProducer, never()).publishClickEvent(any());
        verify(repository, never()).incrementClickCount(shortCode);
    }

    @Test
    void getUrlStats_success() {
        // Arrange
        String shortCode = "stats123";
        UrlEntry entry = new UrlEntry(shortCode, "https://www.example.com", Instant.now(), null, 42L);
        when(repository.findByShortCodeWithStats(shortCode)).thenReturn(Optional.of(entry));

        // Act
        UrlEntry result = service.getUrlStats(shortCode);

        // Assert
        assertNotNull(result);
        assertEquals(42L, result.getClickCount());
        assertEquals(shortCode, result.getShortCode());
        verify(repository, times(1)).findByShortCodeWithStats(shortCode);
    }
}
