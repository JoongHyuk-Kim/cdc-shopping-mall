package com.example.order.domain;

public enum OutboxEventType {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELLED
}
