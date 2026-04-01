package com.example.consumer.listener;

import com.example.consumer.dto.OrderEventPayload;
import com.example.consumer.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryListener {

    private final OutboxEventParser outboxEventParser;
    private final InventoryService inventoryService;

    @KafkaListener(topics = "shopdb.shopdb.outbox_events", groupId = "inventory-group")
    public void handleOrderForInventory(String message) throws Exception {
        OrderEventPayload event = outboxEventParser.parse(message).payload();

        if ("ORDER_CONFIRMED".equals(event.getEventType())) {
            log.info("=== [재고] 주문 확정 → 재고 차감 ===");
            log.info("  주문번호: {}", event.getOrderId());
            log.info("  품목 수: {}", event.getItems() != null ? event.getItems().size() : 0);
            inventoryService.deductStock(event);
            log.info("  → 재고 차감 완료");
        }

        if ("ORDER_CANCELLED".equals(event.getEventType())) {
            log.info("=== [재고] 주문 취소 → 재고 복원 ===");
            log.info("  주문번호: {}", event.getOrderId());
            log.info("  품목 수: {}", event.getItems() != null ? event.getItems().size() : 0);
            inventoryService.restoreStock(event);
            log.info("  → 재고 복원 완료");
        }
    }
}
