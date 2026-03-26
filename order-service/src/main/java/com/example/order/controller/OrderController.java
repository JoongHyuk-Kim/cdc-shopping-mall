package com.example.order.controller;

import com.example.order.domain.Order;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus(),
                "totalAmount", order.getTotalAmount()
        ));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(Map.of("orderId", order.getId(), "status", order.getStatus()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(Map.of("orderId", order.getId(), "status", order.getStatus()));
    }
}
