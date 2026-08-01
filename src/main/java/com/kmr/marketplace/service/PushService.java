package com.kmr.marketplace.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.kmr.marketplace.entity.DeviceToken;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.DeviceTokenRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;

/**
 * Push notifications via Firebase Cloud Messaging (FCM HTTP v1, Admin SDK).
 *
 * Initialises from the service-account JSON at fcm.service-account-json. When that
 * is absent it runs in dev-mode (logs instead of sending), so the flow is testable
 * without Firebase. Device-token registration is always active.
 */
@Service
@Transactional
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final DeviceTokenRepository tokenRepo;

    @Value("${fcm.service-account-json:}")
    private String serviceAccountPath;

    private volatile FirebaseMessaging messaging;   // null until/unless configured

    public PushService(DeviceTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    @PostConstruct
    void init() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.info("FCM in dev-mode (no service account configured — pushes are logged).");
            return;
        }
        try (FileInputStream in = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options)
                    : FirebaseApp.getInstance();
            messaging = FirebaseMessaging.getInstance(app);
            log.info("FCM initialised — push notifications are LIVE.");
        } catch (Exception e) {
            log.warn("FCM init failed ({}). Falling back to dev-mode.", e.getMessage());
        }
    }

    public boolean isLive() { return messaging != null; }

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

    /** Send a notification to every device registered to a user. Never throws. */
    public void sendToUser(Long userId, String title, String body) {
        List<DeviceToken> tokens = tokenRepo.findByUserId(userId);
        if (tokens.isEmpty()) return;

        if (messaging == null) {
            log.info("[push dev-mode] to user {} ({} device(s)): {} — {}",
                    userId, tokens.size(), title, body);
            return;
        }
        for (DeviceToken dt : tokens) {
            try {
                messaging.send(Message.builder()
                        .setToken(dt.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title).setBody(body).build())
                        .putData("type", "order")
                        .build());
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    tokenRepo.deleteByToken(dt.getToken());   // drop stale token
                }
                log.warn("Push to user {} failed ({}): {}", userId, code, e.getMessage());
            } catch (Exception e) {
                log.warn("Push to user {} failed: {}", userId, e.getMessage());
            }
        }
    }
}
