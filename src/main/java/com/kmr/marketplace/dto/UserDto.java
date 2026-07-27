package com.kmr.marketplace.dto;

public record UserDto(
        Long   id,
        String name,
        String email,
        String phone,
        String avatarUrl,
        String role
) {}
