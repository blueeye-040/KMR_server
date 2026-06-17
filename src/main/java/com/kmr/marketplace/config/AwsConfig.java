package com.kmr.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {

    @Value("${aws.access-key-id}")
    private String accessKey;

    @Value("${aws.secret-access-key}")
    private String secretKey;

    @Value("${aws.region:ap-south-1}")
    private String region;

    /**
     * Single shared credentials bean.
     * Any AWS service client (SNS, S3, SES, etc.) injects this —
     * credentials are configured in one place only.
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    // ── Service clients — add more here as needed ─────────────

    @Bean
    public SnsClient snsClient(AwsCredentialsProvider credentialsProvider) {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /*
     * Example — uncomment when you need S3 (product images):
     *
     * @Bean
     * public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
     *     return S3Client.builder()
     *             .region(Region.of(region))
     *             .credentialsProvider(credentialsProvider)
     *             .build();
     * }
     *
     * Example — uncomment when you need SES (order confirmation emails):
     *
     * @Bean
     * public SesClient sesClient(AwsCredentialsProvider credentialsProvider) {
     *     return SesClient.builder()
     *             .region(Region.of(region))
     *             .credentialsProvider(credentialsProvider)
     *             .build();
     * }
     */
}
