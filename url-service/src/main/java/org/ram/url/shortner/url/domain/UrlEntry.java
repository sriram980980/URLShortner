package org.ram.url.shortner.url.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "urls")
public class UrlEntry implements Serializable {

    @Id
    private String id;

    // @Indexed(unique = true) handld at startup ensure idx
    private String shortCode;

    private String originalUrl;

    private Instant createdAt;
    private Instant expiresAt;
    private long clickCount;

    public UrlEntry() {
    }

    public UrlEntry(String shortCode, String originalUrl, Instant createdAt, Instant expiresAt) {
        this(shortCode, originalUrl, createdAt, expiresAt, 0L);
    }

    public UrlEntry(String shortCode, String originalUrl, Instant createdAt, Instant expiresAt, long clickCount) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
    }

    @JsonIgnore
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    // Alias methods for compatibility
    public String getLongUrl() {
        return originalUrl;
    }

    public void setLongUrl(String longUrl) {
        this.originalUrl = longUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }
}
