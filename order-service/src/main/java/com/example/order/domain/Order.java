package com.example.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Order create(String customerName, String customerEmail) {
        Order order = new Order();
        order.customerName = customerName;
        order.customerEmail = customerEmail;
        return order;
    }

    public void addItem(String productName, Long productId, int quantity, BigDecimal unitPrice) {
        OrderItem item = OrderItem.create(this, productName, productId, quantity, unitPrice);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
