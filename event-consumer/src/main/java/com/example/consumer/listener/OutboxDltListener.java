package com.example.consumer.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OutboxDltListener {

    @KafkaListener(topics = "shopdb.shopdb.outbox_events.DLT", groupId = "outbox-dlt-group")
    public void handleDltMessage(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset
    ) {
        log.error("=== [DLT] 처리 실패 메시지 적재 ===");
        log.error("  topic={}, partition={}, offset={}", topic, partition, offset);
        log.error("  payload={}", message);
    }
}
