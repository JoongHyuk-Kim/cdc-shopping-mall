package com.example.consumer.listener;

import com.example.consumer.dto.OrderEventPayload;
import com.example.consumer.dto.OutboxEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventParser {

    private final ObjectMapper objectMapper;

    public ParsedOutboxEvent parse(String message) throws Exception {
        OutboxEventMessage outboxEvent = objectMapper.readValue(message, OutboxEventMessage.class);
        OrderEventPayload payload = objectMapper.readValue(outboxEvent.getPayload(), OrderEventPayload.class);
        return new ParsedOutboxEvent(outboxEvent, payload);
    }

    public record ParsedOutboxEvent(OutboxEventMessage outboxEvent, OrderEventPayload payload) {
    }
}
