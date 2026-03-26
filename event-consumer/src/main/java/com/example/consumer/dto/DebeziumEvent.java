package com.example.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumEvent {

    private Long id;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    private String status;

    @JsonProperty("__op")
    private String operation;

    @JsonProperty("__table")
    private String table;

    @JsonProperty("__source_ts_ms")
    private Long sourceTimestamp;
}
