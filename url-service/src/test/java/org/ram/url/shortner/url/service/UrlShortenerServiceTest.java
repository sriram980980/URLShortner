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

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private ClickEventProducer clickEventProducer;

    @InjectMocks
    private UrlShortenerService service;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8081";
    }

    @Test
    void createShortUrl_success() {
        // Arrange
        String longUrl = "https://www.example.com/very/long/url/path";
        ShortenRequest request = new ShortenRequest(longUrl, 3600L);

        // Act
        ShortenResponse response = service.createShortUrl(request, baseUrl);

        // Assert
        assertNotNull(response.shortCode());
        assertFalse(response.shortCode().isEmpty());
        assertTrue(response.shortUrl().startsWith(baseUrl + "/"));
        assertEquals(longUrl, response.originalUrl());
        assertNotNull(response.expiresAt());
        verify(repository, times(1)).save(any(UrlEntry.class));
    }

    @Test
    void resolveUrl_success() {
        // Arrange
        String shortCode = "abc123";
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
        String shortCode = "expired";
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
}
