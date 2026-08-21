package org.ram.url.shortner.url.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClickEventProducer {
    private static final Logger log = LoggerFactory.getLogger(ClickEventProducer.class);

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    @Value("${kafka.topics.click-events:url.click.events}")
    private String clickEventsTopic;

    public ClickEventProducer(KafkaTemplate<String, ClickEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishClickEvent(ClickEvent event) {
        kafkaTemplate.send(clickEventsTopic, event.shortCode(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish click event for shortCode={}: {}", event.shortCode(), ex.getMessage());
                } else {
                    log.debug("Click event published for shortCode={}", event.shortCode());
                }
            });
    }
}
