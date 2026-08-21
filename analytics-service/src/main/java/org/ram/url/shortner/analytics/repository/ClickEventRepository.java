package org.ram.url.shortner.analytics.repository;

import org.ram.url.shortner.analytics.domain.ClickEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ClickEventRepository extends ElasticsearchRepository<ClickEvent, String> {
    List<ClickEvent> findByShortCode(String shortCode);
    long countByShortCode(String shortCode);
}
