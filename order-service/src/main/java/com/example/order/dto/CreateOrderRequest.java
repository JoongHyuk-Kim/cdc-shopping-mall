package com.example.order.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderRequest {
    @NotBlank
    private String customerName;

    @Email
    @NotBlank
    private String customerEmail;

    @NotEmpty
    private List<OrderItemRequest> items;
}
