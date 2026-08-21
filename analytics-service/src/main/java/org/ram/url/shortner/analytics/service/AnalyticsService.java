package org.ram.url.shortner.analytics.service;

import org.ram.url.shortner.analytics.domain.ClickEvent;
import org.ram.url.shortner.analytics.repository.ClickEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {
    private final ClickEventRepository repository;

    public AnalyticsService(ClickEventRepository repository) {
        this.repository = repository;
    }

    public List<ClickEvent> getClickEvents(String shortCode) {
        return repository.findByShortCode(shortCode);
    }

    public long getClickCount(String shortCode) {
        return repository.countByShortCode(shortCode);
    }

    public Iterable<ClickEvent> getAllEvents() {
        return repository.findAll();
    }
}
