package com.example.consumer.listener;

import com.example.consumer.dto.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryListener {

    private final OutboxEventParser outboxEventParser;

    @KafkaListener(topics = "shopdb.shopdb.outbox_events", groupId = "inventory-group")
    public void handleOrderForInventory(String message) throws Exception {
        OrderEventPayload event = outboxEventParser.parse(message).payload();

        if ("ORDER_CONFIRMED".equals(event.getEventType())) {
            log.info("=== [재고] 주문 확정 → 재고 차감 ===");
            log.info("  주문번호: {}", event.getOrderId());
            log.info("  품목 수: {}", event.getItems() != null ? event.getItems().size() : 0);
            log.info("  → payload 기준으로 재고 차감 처리 시작");
        }

        if ("ORDER_CANCELLED".equals(event.getEventType())) {
            log.info("=== [재고] 주문 취소 → 재고 복원 ===");
            log.info("  주문번호: {}", event.getOrderId());
            log.info("  품목 수: {}", event.getItems() != null ? event.getItems().size() : 0);
            log.info("  → payload 기준으로 재고 복원 처리 시작");
        }
    }
}
