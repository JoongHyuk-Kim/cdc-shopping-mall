package com.example.consumer.listener;

import com.example.consumer.dto.DebeziumEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsListener {

    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"));

    @KafkaListener(topics = "shopdb.shopdb.orders", groupId = "analytics-group")
    public void handleOrderForAnalytics(String message) {
        try {
            DebeziumEvent event = objectMapper.readValue(message, DebeziumEvent.class);

            String eventTime = event.getSourceTimestamp() != null
                    ? FMT.format(Instant.ofEpochMilli(event.getSourceTimestamp()))
                    : "unknown";

            log.info("=== [분석] CDC 이벤트 수집 ===");
            log.info("  op={}, table={}, orderId={}, status={}, amount={}, ts={}",
                    event.getOperation(), event.getTable(),
                    event.getId(), event.getStatus(),
                    event.getTotalAmount(), eventTime);
        } catch (Exception e) {
            log.error("분석 데이터 적재 실패: {}", e.getMessage(), e);
        }
    }
}
