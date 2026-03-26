package com.example.order.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemRequest {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
}
