package com.mindtrack.common.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KmsEncryptionConverterTest {

    @Mock
    private KmsEncryptionService kmsService;

    private KmsEncryptionConverter converter;

    @BeforeEach
    void setUp() throws Exception {
        setStaticInstance(kmsService);
        converter = new KmsEncryptionConverter();
    }

    @AfterEach
    void tearDown() throws Exception {
        setStaticInstance(null);
    }

    // --- convertToDatabaseColumn ---

    @Test
    void convertToDatabaseColumn_nullValue_returnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        verify(kmsService, never()).encrypt(null);
    }

    @Test
    void convertToDatabaseColumn_serviceDisabled_returnsPlaintext() {
        when(kmsService.isEnabled()).thenReturn(false);

        String result = converter.convertToDatabaseColumn("12345");

        assertThat(result).isEqualTo("12345");
        verify(kmsService, never()).encrypt("12345");
    }

    @Test
    void convertToDatabaseColumn_serviceEnabled_encryptsValue() {
        when(kmsService.isEnabled()).thenReturn(true);
        when(kmsService.encrypt("12345")).thenReturn("ENC:abc123");

        String result = converter.convertToDatabaseColumn("12345");

        assertThat(result).isEqualTo("ENC:abc123");
    }

    // --- convertToEntityAttribute ---

    @Test
    void convertToEntityAttribute_nullDbData_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        verify(kmsService, never()).decrypt(null);
    }

    @Test
    void convertToEntityAttribute_serviceDisabled_returnsRawValue() {
        when(kmsService.isEnabled()).thenReturn(false);

        String result = converter.convertToEntityAttribute("ENC:abc123");

        assertThat(result).isEqualTo("ENC:abc123");
        verify(kmsService, never()).decrypt("ENC:abc123");
    }

    @Test
    void convertToEntityAttribute_encryptedValue_decryptsValue() {
        when(kmsService.isEnabled()).thenReturn(true);
        when(kmsService.decrypt("ENC:abc123")).thenReturn("12345");

        String result = converter.convertToEntityAttribute("ENC:abc123");

        assertThat(result).isEqualTo("12345");
    }

    @Test
    void convertToEntityAttribute_legacyPlaintext_returnsAsIs() {
        when(kmsService.isEnabled()).thenReturn(true);

        String result = converter.convertToEntityAttribute("plaintext-no-prefix");

        assertThat(result).isEqualTo("plaintext-no-prefix");
        verify(kmsService, never()).decrypt("plaintext-no-prefix");
    }

    @Test
    void convertToEntityAttribute_noServiceInstance_returnsRawValue() throws Exception {
        setStaticInstance(null);

        String result = converter.convertToEntityAttribute("whatever");

        assertThat(result).isEqualTo("whatever");
    }

    // --- round-trip ---

    @Test
    void roundTrip_encryptThenDecrypt_returnsOriginal() {
        when(kmsService.isEnabled()).thenReturn(true);
        when(kmsService.encrypt("secret")).thenReturn("ENC:enc-secret");
        when(kmsService.decrypt("ENC:enc-secret")).thenReturn("secret");

        String encrypted = converter.convertToDatabaseColumn("secret");
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo("secret");
    }

    // --- helpers ---

    private static void setStaticInstance(KmsEncryptionService value) throws Exception {
        Field field = KmsEncryptionService.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, value);
    }
}
