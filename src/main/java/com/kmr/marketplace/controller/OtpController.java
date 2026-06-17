package com.kmr.marketplace.controller;

import com.kmr.marketplace.security.OtpStore;
import com.kmr.marketplace.service.SmsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class OtpController {

    private final OtpStore otpStore;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpController(OtpStore otpStore, SmsService smsService) {
        this.otpStore   = otpStore;
        this.smsService = smsService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @RequestBody @Validated OtpRequest req) {

        // SecureRandom — cryptographically strong, not Math.random()
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.saveOtp(req.phone(), otp);
        smsService.sendOtp(req.phone(), otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to XXXXXX" + req.phone()
                        .substring(req.phone().length() - 4)
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @RequestBody @Validated VerifyRequest req) {

        otpStore.verifyOtp(req.phone(), req.otp());
        return ResponseEntity.ok(Map.of("message", "Phone verified successfully"));
    }

    public record OtpRequest(
            @NotBlank
            @Pattern(regexp = "^[6-9]\\d{9}$",
                     message = "Enter a valid 10-digit mobile number")
            String phone
    ) {}

    public record VerifyRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "^\\d{6}$",
                               message = "OTP must be 6 digits")
            String otp
    ) {}
}
