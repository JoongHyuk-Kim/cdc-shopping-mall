package com.example.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    public static OrderItem create(Order order, String productName, Long productId,
                                   int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.order = order;
        item.productName = productName;
        item.productId = productId;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }
}
