package com.kmr.marketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cdnDomain;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client  = s3Client;
        this.presigner = presigner;
    }

    /**
     * Upload a file to S3 and return its public CloudFront CDN URL.
     *
     * @param file   the multipart file to upload
     * @param folder e.g. "products", "shops", "banners"
     * @return full CDN URL: https://d1234.cloudfront.net/products/uuid.jpg
     */
    public String upload(MultipartFile file, String folder) throws IOException {
        String ext       = getExtension(file.getOriginalFilename());
        String objectKey = folder + "/" + UUID.randomUUID() + ext;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return cdnUrl(objectKey);
    }

    /**
     * Generate a pre-signed PUT URL so the Flutter client can upload directly
     * to S3 without going through the server (faster, saves bandwidth).
     *
     * @param folder      e.g. "products"
     * @param contentType e.g. "image/jpeg"
     * @return a PresignedUpload containing the upload URL and the final CDN URL
     */
    public PresignedUpload presignUpload(String folder, String contentType) {
        String ext       = contentTypeToExtension(contentType);
        String objectKey = folder + "/" + UUID.randomUUID() + ext;

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(r -> r
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(contentType))
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return new PresignedUpload(presigned.url().toString(), cdnUrl(objectKey));
    }

    /** Delete an object by its CDN or S3 URL. */
    public void delete(String url) {
        String key = extractKey(url);
        if (key == null) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    /** Convert an S3 object key to a CloudFront CDN URL. */
    public String cdnUrl(String objectKey) {
        String base = cdnDomain.endsWith("/")
                ? cdnDomain.substring(0, cdnDomain.length() - 1)
                : cdnDomain;
        return base + "/" + objectKey;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String contentTypeToExtension(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType) {
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            default           -> ".jpg";
        };
    }

    /** Extract the S3 object key from a CDN or S3 URL. Returns null if unrecognised. */
    private String extractKey(String url) {
        if (url == null) return null;
        String base = cdnDomain.endsWith("/")
                ? cdnDomain.substring(0, cdnDomain.length() - 1)
                : cdnDomain;
        if (url.startsWith(base + "/")) {
            return url.substring(base.length() + 1);
        }
        // Fallback: strip s3 bucket URL
        int idx = url.indexOf(".amazonaws.com/");
        return idx >= 0 ? url.substring(idx + ".amazonaws.com/".length()) : null;
    }

    public record PresignedUpload(String uploadUrl, String cdnUrl) {}
}
