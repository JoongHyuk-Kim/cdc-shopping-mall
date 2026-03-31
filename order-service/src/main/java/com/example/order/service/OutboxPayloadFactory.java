package com.example.order.service;

import com.example.order.domain.Order;
import com.example.order.domain.OrderItem;
import com.example.order.domain.OutboxEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPayloadFactory {

    private final ObjectMapper objectMapper;

    public String createOrderPayload(Order order, OutboxEventType eventType) {
        OrderOutboxPayload payload = new OrderOutboxPayload(
                eventType.name(),
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(this::toItemPayload)
                        .toList()
        );

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload 직렬화에 실패했습니다.", e);
        }
    }

    private OrderItemPayload toItemPayload(OrderItem item) {
        return new OrderItemPayload(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice()
        );
    }

    private record OrderOutboxPayload(
            String eventType,
            Long orderId,
            String customerName,
            String customerEmail,
            String status,
            BigDecimal totalAmount,
            List<OrderItemPayload> items
    ) {
    }

    private record OrderItemPayload(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
