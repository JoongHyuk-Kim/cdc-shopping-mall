package com.example.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxEventMessage {

    private Long id;

    @JsonProperty("aggregate_type")
    private String aggregateType;

    @JsonProperty("aggregate_id")
    private String aggregateId;

    @JsonProperty("event_type")
    private String eventType;

    private String payload;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("__op")
    private String operation;

    @JsonProperty("__table")
    private String table;

    @JsonProperty("__source_ts_ms")
    private Long sourceTimestamp;
}
