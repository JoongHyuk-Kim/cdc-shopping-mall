package com.example.consumer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderEventPayload {

    private String eventType;
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemPayload> items;

    @Data
    public static class OrderItemPayload {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
