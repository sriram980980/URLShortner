package org.ram.url.shortner.analytics.consumer;

import org.ram.url.shortner.analytics.domain.ClickEvent;
import org.ram.url.shortner.analytics.dto.ClickEventMessage;
import org.ram.url.shortner.analytics.repository.ClickEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClickEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);

    private final ClickEventRepository repository;

    public ClickEventConsumer(ClickEventRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
        topics = "${kafka.topics.click-events:url.click.events}",
        groupId = "${spring.kafka.consumer.group-id:analytics-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ClickEventMessage message) {
        log.info("Received click event: shortCode={}, ip={}", message.shortCode(), message.ipAddress());
        try {
            ClickEvent event = new ClickEvent(
                message.shortCode(),
                message.longUrl(),
                message.timestamp(),
                message.userAgent(),
                message.ipAddress()
            );
            repository.save(event);
            log.debug("Saved click event to Elasticsearch: shortCode={}", message.shortCode());
        } catch (Exception e) {
            log.error("Failed to process click event for shortCode={}: {}", message.shortCode(), e.getMessage(), e);
        }
    }
}
