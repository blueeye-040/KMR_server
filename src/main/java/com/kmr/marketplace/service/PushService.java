package com.kmr.marketplace.service;

import com.kmr.marketplace.entity.DeviceToken;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Push notifications via FCM.
 *
 * Registration is fully working (device tokens are stored per user). Actual
 * dispatch runs in dev-mode until a Firebase service-account JSON is provided
 * (`fcm.service-account-json` / env FCM_SERVICE_ACCOUNT_JSON) — then wire the
 * FCM HTTP v1 send here. Dev-mode logs the notification so flows are testable.
 */
@Service
@Transactional
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final DeviceTokenRepository tokenRepo;

    @Value("${fcm.service-account-json:}")
    private String serviceAccountJson;

    public PushService(DeviceTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public boolean isLive() {
        return serviceAccountJson != null && !serviceAccountJson.isBlank();
    }

    public void registerToken(User user, String token, String platform) {
        DeviceToken existing = tokenRepo.findByToken(token).orElse(null);
        if (existing != null) {
            existing.setUser(user);
            existing.setPlatform(platform);
            tokenRepo.save(existing);
            return;
        }
        DeviceToken dt = new DeviceToken();
        dt.setUser(user);
        dt.setToken(token);
        dt.setPlatform(platform);
        tokenRepo.save(dt);
    }

    public void unregisterToken(String token) {
        tokenRepo.deleteByToken(token);
    }

    /** Send a notification to every device registered to a user. */
    public void sendToUser(Long userId, String title, String body) {
        List<DeviceToken> tokens = tokenRepo.findByUserId(userId);
        if (tokens.isEmpty()) return;
        if (!isLive()) {
            log.info("[push dev-mode] to user {} ({} device(s)): {} — {}",
                    userId, tokens.size(), title, body);
            return;
        }
        // TODO: FCM HTTP v1 send using the service account (bearer token) for each token.
        log.info("[push] would send to user {} ({} device(s)): {}", userId, tokens.size(), title);
    }
}
