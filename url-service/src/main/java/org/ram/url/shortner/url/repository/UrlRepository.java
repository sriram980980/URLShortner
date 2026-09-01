package org.ram.url.shortner.url.repository;

import org.ram.url.shortner.url.domain.UrlEntry;
import org.ram.url.shortner.url.domain.UrlSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UrlRepository {

    private static final Logger log = LoggerFactory.getLogger(UrlRepository.class);
    private static final String SEQUENCE_ID = "url_sequence";

    private final MongoTemplate mongoTemplate;

    public UrlRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Saves a URL entry in MongoDB.
     * Throws DuplicateKeyException if a unique index constraint is violated.
     */
    public UrlEntry save(UrlEntry entry) {
        log.debug("Saving URL entry: shortCode={}, originalUrl={}", entry.getShortCode(), entry.getOriginalUrl());
        return mongoTemplate.save(entry);
    }

    /**
     * Finds a URL entry by its unique shortCode.
     */
    public Optional<UrlEntry> findByShortCode(String shortCode) {
        Query query = Query.query(Criteria.where("shortCode").is(shortCode));
        UrlEntry entry = mongoTemplate.findOne(query, UrlEntry.class);
        return Optional.ofNullable(entry);
    }

    /**
     * Finds a URL entry by its unique originalUrl.
     */
    public Optional<UrlEntry> findByOriginalUrl(String originalUrl) {
        Query query = Query.query(Criteria.where("originalUrl").is(originalUrl));
        UrlEntry entry = mongoTemplate.findOne(query, UrlEntry.class);
        return Optional.ofNullable(entry);
    }

    /**
     * Finds a URL entry with its current statistics.
     */
    public Optional<UrlEntry> findByShortCodeWithStats(String shortCode) {
        return findByShortCode(shortCode);
    }

    /**
     * Atomically increments and returns the sequence counter in the url_sequence collection.
     */
    public long nextId() {
        Query query = Query.query(Criteria.where("_id").is(SEQUENCE_ID));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
        UrlSequence seq = mongoTemplate.findAndModify(query, update, options, UrlSequence.class);

        if (seq == null) {
            throw new IllegalStateException("Failed to generate sequence ID from MongoDB counter");
        }
        return seq.getSeq();
    }

    /**
     * Atomically increments the clickCount field for the given shortCode in MongoDB.
     */
    public void incrementClickCount(String shortCode) {
        Query query = Query.query(Criteria.where("shortCode").is(shortCode));
        Update update = new Update().inc("clickCount", 1);
        mongoTemplate.updateFirst(query, update, UrlEntry.class);
        log.debug("Incremented click count for shortCode={}", shortCode);
    }

    /**
     * Returns the click count for a shortCode.
     */
    public long getClickCount(String shortCode) {
        return findByShortCode(shortCode)
                .map(UrlEntry::getClickCount)
                .orElse(0L);
    }
}
