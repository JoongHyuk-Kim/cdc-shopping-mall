package com.example.consumer.listener;

import com.example.consumer.dto.DebeziumEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "shopdb.shopdb.orders", groupId = "inventory-group")
    public void handleOrderForInventory(String message) {
        try {
            DebeziumEvent event = objectMapper.readValue(message, DebeziumEvent.class);

            if ("u".equals(event.getOperation()) && "CONFIRMED".equals(event.getStatus())) {
                log.info("=== [재고] 주문 확정 → 재고 차감 ===");
                log.info("  주문번호: {}", event.getId());
                log.info("  → 재고 차감 처리 시작 (order_items 조회 후 product_inventory 차감)");
            }

            if ("u".equals(event.getOperation()) && "CANCELLED".equals(event.getStatus())) {
                log.info("=== [재고] 주문 취소 → 재고 복원 ===");
                log.info("  주문번호: {}", event.getId());
                log.info("  → 재고 복원 처리 시작");
            }
        } catch (Exception e) {
            log.error("재고 처리 실패: {}", e.getMessage(), e);
        }
    }
}
