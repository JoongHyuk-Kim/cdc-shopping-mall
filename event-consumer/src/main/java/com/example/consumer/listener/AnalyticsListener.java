package com.example.consumer.listener;

import com.example.consumer.dto.OrderEventPayload;
import com.example.consumer.dto.OutboxEventMessage;
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

    private final OutboxEventParser outboxEventParser;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"));

    @KafkaListener(topics = "shopdb.shopdb.outbox_events", groupId = "analytics-group")
    public void handleOrderForAnalytics(String message) {
        try {
            OutboxEventParser.ParsedOutboxEvent parsed = outboxEventParser.parse(message);
            OutboxEventMessage outboxEvent = parsed.outboxEvent();
            OrderEventPayload payload = parsed.payload();

            String eventTime = outboxEvent.getSourceTimestamp() != null
                    ? FMT.format(Instant.ofEpochMilli(outboxEvent.getSourceTimestamp()))
                    : "unknown";

            log.info("=== [분석] Outbox 이벤트 수집 ===");
            log.info("  op={}, table={}, eventType={}, aggregateId={}, orderId={}, status={}, amount={}, ts={}",
                    outboxEvent.getOperation(), outboxEvent.getTable(),
                    outboxEvent.getEventType(), outboxEvent.getAggregateId(),
                    payload.getOrderId(), payload.getStatus(),
                    payload.getTotalAmount(), eventTime);
        } catch (Exception e) {
            log.error("분석 데이터 적재 실패: {}", e.getMessage(), e);
        }
    }
}
