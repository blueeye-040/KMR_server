package com.kmr.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

/** identifier = the user's email or 10-digit phone. */
public record ForgotPasswordRequest(
        @NotBlank String identifier
) {}
