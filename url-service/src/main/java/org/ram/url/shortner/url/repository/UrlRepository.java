package org.ram.url.shortner.url.repository;

import org.ram.url.shortner.url.domain.UrlEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
public class UrlRepository {
    private static final Logger log = LoggerFactory.getLogger(UrlRepository.class);
    private static final String URL_PREFIX = "url:";
    private static final String CLICKS_PREFIX = "clicks:";

    private final RedisTemplate<String, UrlEntry> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public UrlRepository(RedisTemplate<String, UrlEntry> redisTemplate,
                         StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void save(UrlEntry entry) {
        String key = URL_PREFIX + entry.getShortCode();
        redisTemplate.opsForValue().set(key, entry);
        if (entry.getExpiresAt() != null) {
            Duration ttl = Duration.between(Instant.now(), entry.getExpiresAt());
            if (!ttl.isNegative()) {
                redisTemplate.expire(key, ttl);
            }
        }
        log.debug("Saved URL entry: shortCode={}", entry.getShortCode());
    }

    public Optional<UrlEntry> findByShortCode(String shortCode) {
        UrlEntry entry = redisTemplate.opsForValue().get(URL_PREFIX + shortCode);
        return Optional.ofNullable(entry);
    }

    public Optional<UrlEntry> findByShortCodeWithStats(String shortCode) {
        return findByShortCode(shortCode).map(entry -> {
            entry.setClickCount(getClickCount(shortCode));
            return entry;
        });
    }

    // Native Redis INCR — atomic, O(1), no fetch/deserialize/re-serialize cycle
    public void incrementClickCount(String shortCode) {
        stringRedisTemplate.opsForValue().increment(CLICKS_PREFIX + shortCode);
        log.debug("INCR clicks:{}", shortCode);
    }

    public long getClickCount(String shortCode) {
        String raw = stringRedisTemplate.opsForValue().get(CLICKS_PREFIX + shortCode);
        return raw != null ? Long.parseLong(raw) : 0L;
    }
}
