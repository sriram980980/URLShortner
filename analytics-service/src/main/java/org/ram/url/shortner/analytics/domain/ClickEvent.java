package org.ram.url.shortner.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "click-events")
public class ClickEvent {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String shortCode;

    @Field(type = FieldType.Text)
    private String longUrl;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Text)
    private String userAgent;

    @Field(type = FieldType.Keyword)
    private String ipAddress;

    public ClickEvent() {}

    public ClickEvent(String shortCode, String longUrl, Instant timestamp, String userAgent, String ipAddress) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.timestamp = timestamp;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
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

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
