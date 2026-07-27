package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank String token,
        String platform   // ANDROID | IOS
) {}
