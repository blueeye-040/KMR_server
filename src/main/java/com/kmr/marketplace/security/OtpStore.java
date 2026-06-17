package com.kmr.marketplace.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OtpStore {

    private record Entry(
            String hashedOtp,
            Instant expiry,
            AtomicInteger verifyAttempts,
            boolean verified,
            Instant resendWindowStart,
            AtomicInteger resendCount
    ) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    public void saveOtp(String phone, String plainOtp) {
        Entry existing = store.get(phone);
        AtomicInteger resendCount;
        Instant resendWindowStart;

        if (existing != null) {
            boolean windowExpired = Instant.now()
                    .isAfter(existing.resendWindowStart().plusSeconds(600));
            if (windowExpired) {
                resendCount = new AtomicInteger(1);
                resendWindowStart = Instant.now();
            } else {
                int count = existing.resendCount().incrementAndGet();
                if (count > 3) throw new RateLimitException(
                        "Too many OTP requests. Wait 10 minutes.");
                resendCount = existing.resendCount();
                resendWindowStart = existing.resendWindowStart();
            }
        } else {
            resendCount = new AtomicInteger(1);
            resendWindowStart = Instant.now();
        }

        store.put(phone, new Entry(
                hash(plainOtp),
                Instant.now().plusSeconds(300),
                new AtomicInteger(0),
                false,
                resendWindowStart,
                resendCount
        ));
    }

    public boolean verifyOtp(String phone, String plainOtp) {
        Entry entry = store.get(phone);

        if (entry == null)
            throw new OtpException("No OTP found. Request a new one.");
        if (Instant.now().isAfter(entry.expiry())) {
            store.remove(phone);
            throw new OtpException("OTP expired. Request a new one.");
        }

        int attempts = entry.verifyAttempts().incrementAndGet();
        if (attempts > 3) {
            store.remove(phone);
            throw new RateLimitException("Too many wrong attempts. Request a new OTP.");
        }

        if (!entry.hashedOtp().equals(hash(plainOtp))) {
            int left = 3 - attempts;
            throw new OtpException("Wrong OTP. " + left + " attempt(s) left.");
        }

        store.put(phone, new Entry(
                entry.hashedOtp(),
                Instant.now().plusSeconds(300),
                entry.verifyAttempts(),
                true,
                entry.resendWindowStart(),
                entry.resendCount()
        ));
        return true;
    }

    public boolean isOtpVerified(String phone) {
        Entry entry = store.get(phone);
        if (entry == null || !entry.verified()) return false;
        if (Instant.now().isAfter(entry.expiry())) {
            store.remove(phone);
            return false;
        }
        return true;
    }

    public void remove(String phone) {
        store.remove(phone);
    }

    private String hash(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Scheduled(fixedRate = 3_600_000)
    public void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiry()));
    }

    public static class OtpException extends RuntimeException {
        public OtpException(String msg) { super(msg); }
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String msg) { super(msg); }
    }
}
