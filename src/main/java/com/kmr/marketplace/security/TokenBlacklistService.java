package com.kmr.marketplace.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory JWT blacklist. Tokens added on logout are rejected until they expire,
 * then cleaned up automatically by the scheduled task.
 */
@Service
public class TokenBlacklistService {

    // token → expiry instant
    private final ConcurrentHashMap<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant expiry = blacklist.get(token);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /** Runs every hour, removes all tokens that have already expired. */
    @Scheduled(fixedRate = 3_600_000)
    public void purgeExpired() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
