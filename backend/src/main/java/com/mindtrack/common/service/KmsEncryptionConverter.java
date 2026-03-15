package com.mindtrack.common.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA {@link AttributeConverter} that transparently encrypts and decrypts PII column values
 * using {@link KmsEncryptionService}.
 *
 * <p>When encryption is disabled (no {@code ENCRYPTION_KEY_ARN} set, e.g. local / H2 profile)
 * values are stored and returned as-is so that local development and unit tests work without
 * any AWS credentials.
 *
 * <p>Encrypted values are stored in the database as {@code "ENC:<base64-ciphertext>"}.
 * Un-prefixed values (legacy plaintext or local profile) are returned as-is on read so that
 * the {@link com.mindtrack.profile.service.PiiEncryptionMigrationRunner} can re-save them
 * encrypted on the first deployment.
 *
 * <p>Apply to a JPA field with {@code @Convert(converter = KmsEncryptionConverter.class)}.
 */
@Converter
public class KmsEncryptionConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        KmsEncryptionService svc = KmsEncryptionService.getInstance();
        if (svc == null || !svc.isEnabled()) {
            return attribute;
        }
        return svc.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        KmsEncryptionService svc = KmsEncryptionService.getInstance();
        if (svc == null || !svc.isEnabled()) {
            return dbData;
        }
        if (dbData.startsWith(KmsEncryptionService.ENC_PREFIX)) {
            return svc.decrypt(dbData);
        }
        // Legacy plaintext row — return as-is; will be encrypted on next save
        return dbData;
    }
}
