package com.kmr.marketplace.dto;

public record AuthResponse(
    String token,
    String name,
    String email,
    String role
) {}
