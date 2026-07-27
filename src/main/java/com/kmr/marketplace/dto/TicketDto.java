package com.kmr.marketplace.dto;

public record TicketDto(
        Long   id,
        Long   orderId,
        String type,
        String subject,
        String message,
        String status,
        String createdAt
) {}
