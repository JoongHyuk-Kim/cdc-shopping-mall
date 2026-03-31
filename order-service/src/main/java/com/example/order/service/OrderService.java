package com.example.order.service;

import com.example.order.domain.Order;
import com.example.order.domain.OutboxEvent;
import com.example.order.domain.OutboxEventType;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.repository.OutboxEventRepository;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxPayloadFactory outboxPayloadFactory;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.create(request.getCustomerName(), request.getCustomerEmail());

        request.getItems().forEach(item ->
                order.addItem(item.getProductName(), item.getProductId(),
                        item.getQuantity(), item.getUnitPrice())
        );

        Order saved = orderRepository.save(order);
        publishOutboxEvent(saved, OutboxEventType.ORDER_CREATED);
        log.info("주문 생성 완료: orderId={}, customer={}, total={}",
                saved.getId(), saved.getCustomerName(), saved.getTotalAmount());
        return saved;
    }

    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        order.confirm();
        publishOutboxEvent(order, OutboxEventType.ORDER_CONFIRMED);
        log.info("주문 확정: orderId={}, status={}", order.getId(), order.getStatus());
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        order.cancel();
        publishOutboxEvent(order, OutboxEventType.ORDER_CANCELLED);
        log.info("주문 취소: orderId={}, status={}", order.getId(), order.getStatus());
        return order;
    }

    private void publishOutboxEvent(Order order, OutboxEventType eventType) {
        OutboxEvent outboxEvent = OutboxEvent.create(
                "ORDER",
                String.valueOf(order.getId()),
                eventType.name(),
                outboxPayloadFactory.createOrderPayload(order, eventType)
        );
        outboxEventRepository.save(outboxEvent);
    }
}
