package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateTicketRequest(
        Long orderId,
        @Pattern(regexp = "ISSUE|RETURN|EXCHANGE", message = "Invalid type")
        String type,
        @NotBlank String subject,
        String message
) {}
