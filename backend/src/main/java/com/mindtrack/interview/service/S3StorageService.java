package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import java.io.InputStream;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * S3-based storage service for production environments.
 */
@Service
@Profile("!local")
public class S3StorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner,
                            StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.storageProperties = storageProperties;
    }

    @Override
    public void upload(String key, InputStream inputStream, String contentType, long fileSize) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, fileSize));
        LOG.info("Uploaded file to S3: {}/{}", storageProperties.getBucketName(), key);
    }

    @Override
    public String generateAccessUrl(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(storageProperties.getPresignedUrlExpiryMinutes()))
                .getObjectRequest(getRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        LOG.debug("Generated presigned URL for key: {}", key);
        return presigned.url().toString();
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        LOG.info("Deleted file from S3: {}/{}", storageProperties.getBucketName(), key);
    }
}
