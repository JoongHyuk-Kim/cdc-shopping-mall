package com.example.consumer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisEventDeduplicationService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.event-deduplication.processed-ttl:PT72H}")
    private Duration processedTtl;

    @Value("${app.event-deduplication.lock-ttl:PT5M}")
    private Duration lockTtl;

    public boolean tryStartProcessing(String consumerName, Long eventId) {
        if (isAlreadyProcessed(consumerName, eventId)) {
            return false;
        }

        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(processingKey(consumerName, eventId), "1", lockTtl)
        );
    }

    public boolean isAlreadyProcessed(String consumerName, Long eventId) {
        Boolean hasProcessed = stringRedisTemplate.hasKey(processedKey(consumerName, eventId));
        return Boolean.TRUE.equals(hasProcessed);
    }

    public void markProcessed(String consumerName, Long eventId) {
        stringRedisTemplate.opsForValue().set(processedKey(consumerName, eventId), "1", processedTtl);
        stringRedisTemplate.delete(processingKey(consumerName, eventId));
    }

    public void clearProcessing(String consumerName, Long eventId) {
        stringRedisTemplate.delete(processingKey(consumerName, eventId));
    }

    private String processingKey(String consumerName, Long eventId) {
        return "event-processing:" + consumerName + ":" + eventId;
    }

    private String processedKey(String consumerName, Long eventId) {
        return "event-processed:" + consumerName + ":" + eventId;
    }
}
