package com.mindtrack.common.service;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

/**
 * Encrypts and decrypts PII column values via AWS KMS.
 *
 * <p>When {@code ENCRYPTION_KEY_ARN} is not set (local / test profile) the service is disabled
 * and all operations are pass-through, so H2-based tests work without any AWS credentials.
 *
 * <p>Exposes a static accessor so {@link KmsEncryptionConverter}, which is instantiated by
 * JPA/Hibernate outside the Spring container, can reach the live bean.
 */
@Component
public class KmsEncryptionService {

    /** Prefix prepended to every encrypted value stored in the database. */
    static final String ENC_PREFIX = "ENC:";

    // Holder accessed by KmsEncryptionConverter (not a Spring bean)
    private static KmsEncryptionService instance;

    private final String keyArn;
    private KmsClient kmsClient;

    /**
     * Creates the service.
     *
     * @param keyArn ARN of the KMS key; blank when encryption is disabled
     */
    public KmsEncryptionService(@Value("${mindtrack.encryption.key-arn:}") String keyArn) {
        this.keyArn = keyArn;
    }

    @PostConstruct
    void init() {
        if (isEnabled()) {
            this.kmsClient = KmsClient.create();
        }
        instance = this;
    }

    /**
     * Returns the singleton instance for use by {@link KmsEncryptionConverter}.
     *
     * @return the live service bean, or {@code null} if the Spring context is not yet ready
     */
    public static KmsEncryptionService getInstance() {
        return instance;
    }

    /** Returns {@code true} when a KMS key ARN is configured. */
    public boolean isEnabled() {
        return keyArn != null && !keyArn.isBlank();
    }

    /**
     * Encrypts a plaintext string using KMS and returns {@code "ENC:<base64>"}.
     *
     * @param plaintext the value to encrypt
     * @return the encrypted, base64-encoded value prefixed with {@code ENC:}
     */
    public String encrypt(String plaintext) {
        EncryptResponse response = kmsClient.encrypt(EncryptRequest.builder()
                .keyId(keyArn)
                .plaintext(SdkBytes.fromUtf8String(plaintext))
                .build());
        return ENC_PREFIX + Base64.getEncoder().encodeToString(response.ciphertextBlob().asByteArray());
    }

    /**
     * Decrypts a value previously produced by {@link #encrypt}.
     *
     * @param encryptedValue the value in {@code "ENC:<base64>"} format
     * @return the original plaintext string
     */
    public String decrypt(String encryptedValue) {
        String base64 = encryptedValue.substring(ENC_PREFIX.length());
        byte[] ciphertextBytes = Base64.getDecoder().decode(base64);
        DecryptResponse response = kmsClient.decrypt(DecryptRequest.builder()
                .keyId(keyArn)
                .ciphertextBlob(SdkBytes.fromByteArray(ciphertextBytes))
                .build());
        return response.plaintext().asUtf8String();
    }
}
