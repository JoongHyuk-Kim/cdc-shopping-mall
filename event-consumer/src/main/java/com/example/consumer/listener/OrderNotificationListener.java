package com.example.consumer.listener;

import com.example.consumer.dto.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final OutboxEventParser outboxEventParser;

    @KafkaListener(topics = "shopdb.shopdb.outbox_events", groupId = "notification-group1")
    public void handleOrderEvent(String message) throws Exception {
        OrderEventPayload event = outboxEventParser.parse(message).payload();

        switch (event.getEventType()) {
            case "ORDER_CREATED" -> handleNewOrder(event);
            case "ORDER_CONFIRMED", "ORDER_CANCELLED" -> handleOrderUpdate(event);
            default -> log.warn("알 수 없는 eventType: {}", event.getEventType());
        }
    }

    private void handleNewOrder(OrderEventPayload event) {
        log.info("=== [알림] 새 주문 접수 ===");
        log.info("  주문번호: {}", event.getOrderId());
        log.info("  고객: {} ({})", event.getCustomerName(), event.getCustomerEmail());
        log.info("  금액: {}원", event.getTotalAmount());
        log.info("  → {} 님에게 주문 확인 이메일 발송!", event.getCustomerEmail());
    }

    private void handleOrderUpdate(OrderEventPayload event) {
        log.info("=== [알림] 주문 상태 변경 ===");
        log.info("  주문번호: {}, 상태: {}", event.getOrderId(), event.getStatus());

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

}
