package com.kmr.marketplace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Transactional email via AWS SES.
 *
 * Runs in dev-mode (logs instead of sending) until a verified sender is set in
 * `app.mail.from` (env MAIL_FROM). Once set, it sends through SES with the same
 * AWS credentials used elsewhere. Failures never block the calling flow.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SesClient sesClient;

    @Value("${app.mail.from:}")
    private String from;

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    public boolean isLive() {
        return from != null && !from.isBlank();
    }

    public void send(String to, String subject, String bodyText) {
        if (to == null || to.isBlank()) return;
        if (!isLive()) {
            log.info("[email dev-mode] to {} | {} | {}", to, subject, bodyText);
            return;
        }
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(from)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(bodyText).build())
                                    .build())
                            .build())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderConfirmation(String to, String name, String orderNumber, double total) {
        String subject = "KMR Marketplace — Order " + orderNumber + " confirmed";
        String body = "Hi " + (name == null ? "there" : name) + ",\n\n"
                + "Thank you for your order!\n"
                + "Order number: " + orderNumber + "\n"
                + "Total: ₹" + String.format("%,.0f", total) + "\n\n"
                + "You can track it anytime under 'My Orders' in the KMR app.\n\n"
                + "— Team KMR Marketplace";
        send(to, subject, body);
    }
}
