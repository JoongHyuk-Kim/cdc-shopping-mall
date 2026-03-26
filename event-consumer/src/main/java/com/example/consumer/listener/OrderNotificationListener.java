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
public class OrderNotificationListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "shopdb.shopdb.orders", groupId = "notification-group")
    public void handleOrderEvent(String message) {
        try {
            DebeziumEvent event = objectMapper.readValue(message, DebeziumEvent.class);

            switch (event.getOperation()) {
                case "c" -> handleNewOrder(event);
                case "u" -> handleOrderUpdate(event);
                case "d" -> handleOrderDelete(event);
                default -> log.warn("알 수 없는 operation: {}", event.getOperation());
            }
        } catch (Exception e) {
            log.error("알림 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleNewOrder(DebeziumEvent event) {
        log.info("=== [알림] 새 주문 접수 ===");
        log.info("  주문번호: {}", event.getId());
        log.info("  고객: {} ({})", event.getCustomerName(), event.getCustomerEmail());
        log.info("  금액: {}원", event.getTotalAmount());
        log.info("  → {} 님에게 주문 확인 이메일 발송!", event.getCustomerEmail());
    }

    private void handleOrderUpdate(DebeziumEvent event) {
        log.info("=== [알림] 주문 상태 변경 ===");
        log.info("  주문번호: {}, 상태: {}", event.getId(), event.getStatus());

        switch (event.getStatus()) {
            case "CONFIRMED" ->
                    log.info("  → {} 님에게 주문 확정 알림 발송!", event.getCustomerEmail());
            case "CANCELLED" ->
                    log.info("  → {} 님에게 주문 취소 알림 발송!", event.getCustomerEmail());
            case "SHIPPED" ->
                    log.info("  → {} 님에게 배송 시작 알림 발송!", event.getCustomerEmail());
            default ->
                    log.info("  → 상태 변경 알림: {}", event.getStatus());
        }
    }

    private void handleOrderDelete(DebeziumEvent event) {
        log.info("=== [알림] 주문 삭제됨: {} ===", event.getId());
    }
}
