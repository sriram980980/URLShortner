package org.ram.url.shortner.url.config;

import org.ram.url.shortner.url.domain.UrlEntry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndices() {
        mongoTemplate.indexOps(UrlEntry.class)
                .ensureIndex(new Index().on("shortCode", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(UrlEntry.class)
                .ensureIndex(new Index().on("originalUrl", Sort.Direction.ASC).unique());
    }
}
