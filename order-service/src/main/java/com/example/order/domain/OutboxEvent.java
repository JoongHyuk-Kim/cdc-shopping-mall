package com.example.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    // Outbox 레코드 자체의 PK. Debezium이 변경 이벤트를 순서대로 추적할 때 기준이 된다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    // 어떤 Aggregate에서 발생한 이벤트인지 구분한다. 예: ORDER
    @Column(nullable = false)
    private String aggregateType;

    // 이벤트를 발생시킨 Aggregate 식별자. 현재는 주문 ID를 문자열로 저장한다.
    @Column(nullable = false)
    private String aggregateId;

    // 도메인 이벤트 종류. 예: ORDER_CREATED, ORDER_CONFIRMED
    @Column(nullable = false)
    private String eventType;

    // 컨슈머가 실제로 처리할 비즈니스 이벤트 본문(JSON)
    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    // Outbox 레코드가 저장된 시각. CDC 발행 시점 추적에 사용한다.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static OutboxEvent create(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.aggregateType = aggregateType;
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = eventType;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
