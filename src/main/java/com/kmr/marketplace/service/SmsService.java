package com.kmr.marketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;

@Service
public class SmsService {

    private final SnsClient snsClient;

    @Value("${app.sms.dev-mode:true}")
    private boolean devMode;

    public SmsService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendOtp(String phone, String otp) {
        sendText(phone, "Your Valley Rush OTP is: " + otp + ". Valid for 5 minutes. Do not share.");
    }

    /** Send an arbitrary transactional SMS (OTP, reset code, order updates). */
    public void sendText(String phone, String message) {
        if (devMode) {
            System.out.println("============================================");
            System.out.println("  [DEV] SMS to " + phone + " → " + message);
            System.out.println("============================================");
            return;
        }

        String e164 = phone.startsWith("+") ? phone : "+91" + phone;

        snsClient.publish(PublishRequest.builder()
                .phoneNumber(e164)
                .message(message)
                .messageAttributes(Map.of(
                        "AWS.SNS.SMS.SMSType",
                        MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue("Transactional")
                                .build()
                ))
                .build());
    }
}
