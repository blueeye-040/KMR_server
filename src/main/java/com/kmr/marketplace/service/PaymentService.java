package com.kmr.marketplace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Razorpay integration with a dev-mode fallback.
 *
 * When razorpay.key-id / razorpay.key-secret are configured, this calls the
 * Razorpay Orders API over HTTPS (HTTP Basic auth) and verifies the payment
 * signature with HMAC-SHA256 — no external SDK required.
 *
 * When credentials are absent (local/dev), it simulates a Razorpay order and
 * treats every verification as successful, so the full checkout flow can be
 * exercised end-to-end without live keys. Drop real keys into .env to switch.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final HttpClient http = HttpClient.newHttpClient();

    private static final String RAZORPAY_API = "https://api.razorpay.com/v1";

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    /** HOLD = hold vendor money until delivered, then release. SPOT = split immediately. */
    @Value("${razorpay.split-mode:HOLD}")
    private String splitMode;

    /** True only when real Razorpay credentials are present. */
    public boolean isLive() {
        return keyId != null && !keyId.isBlank()
                && keySecret != null && !keySecret.isBlank();
    }

    public String keyId() { return keyId; }

    public String splitMode() { return splitMode; }

    public boolean holdMode() { return "HOLD".equalsIgnoreCase(splitMode); }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create a Route transfer of a vendor's share from a captured payment to their
     * linked account. onHold=true keeps the money on hold until released. Returns
     * the transfer id, or a simulated id in dev-mode.
     */
    public String createTransfer(String paymentId, String linkedAccountId,
                                 BigDecimal amountInr, boolean onHold, String reference) {
        if (!isLive()) {
            String fake = "trf_dev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            log.info("[route dev-mode] transfer {} ₹{} → {} (onHold={}) ref={}",
                    fake, amountInr, linkedAccountId, onHold, reference);
            return fake;
        }
        long paise = amountInr.multiply(BigDecimal.valueOf(100)).longValueExact();
        String body = String.format(
                "{\"transfers\":[{\"account\":\"%s\",\"amount\":%d,\"currency\":\"INR\",\"on_hold\":%s}]}",
                linkedAccountId, paise, onHold ? "1" : "0");
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(RAZORPAY_API + "/payments/" + paymentId + "/transfers"))
                    .header("Authorization", basicAuth())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("Razorpay transfer error " + res.statusCode() + ": " + res.body());
            }
            Matcher m = Pattern.compile("\"id\"\\s*:\\s*\"(trf_[^\"]+)\"").matcher(res.body());
            if (!m.find()) throw new IllegalStateException("No transfer id in response");
            return m.group(1);
        } catch (Exception e) {
            log.warn("Failed to create transfer to {}: {}", linkedAccountId, e.getMessage());
            return null;
        }
    }

    /** Release a held transfer to the vendor (on_hold → 0). */
    public boolean releaseTransfer(String transferId) {
        if (transferId == null) return false;
        if (!isLive()) {
            log.info("[route dev-mode] released transfer {}", transferId);
            return true;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(RAZORPAY_API + "/transfers/" + transferId))
                    .header("Authorization", basicAuth())
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"on_hold\":0}"))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() / 100 == 2;
        } catch (Exception e) {
            log.warn("Failed to release transfer {}: {}", transferId, e.getMessage());
            return false;
        }
    }

    /**
     * Create a payment order at the gateway and return its id.
     * amount is in INR (rupees); Razorpay expects paise.
     */
    public String createOrder(BigDecimal amountInr, String receipt) {
        if (!isLive()) {
            String fake = "order_dev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("[dev-mode] Simulated Razorpay order {} for ₹{}", fake, amountInr);
            return fake;
        }
        long paise = amountInr.multiply(BigDecimal.valueOf(100)).longValueExact();
        String body = String.format(
                "{\"amount\":%d,\"currency\":\"INR\",\"receipt\":\"%s\",\"payment_capture\":1}",
                paise, receipt);
        String auth = Base64.getEncoder().encodeToString(
                (keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(RAZORPAY_ORDERS_URL))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("Razorpay error " + res.statusCode() + ": " + res.body());
            }
            Matcher m = Pattern.compile("\"id\"\\s*:\\s*\"(order_[^\"]+)\"").matcher(res.body());
            if (!m.find()) throw new IllegalStateException("No order id in Razorpay response");
            return m.group(1);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    /**
     * Verify the payment signature returned by Razorpay checkout.
     * signature == HMAC_SHA256(razorpayOrderId + "|" + razorpayPaymentId, keySecret).
     * In dev mode any non-empty paymentId is accepted.
     */
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        if (!isLive()) {
            return razorpayPaymentId != null && !razorpayPaymentId.isBlank();
        }
        if (signature == null || signature.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((razorpayOrderId + "|" + razorpayPaymentId)
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString().equals(signature);
        } catch (Exception e) {
            log.warn("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
