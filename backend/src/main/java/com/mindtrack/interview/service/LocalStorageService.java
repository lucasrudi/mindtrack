package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Local file system storage service for development.
 * Stores files in a temporary directory and returns file:// URLs.
 */
@Service
@Profile("local")
public class LocalStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path storagePath;
    private final Path canonicalBase;

    public LocalStorageService(StorageProperties storageProperties) {
        this.storagePath = Paths.get(storageProperties.getLocalStoragePath());
        this.canonicalBase = this.storagePath.normalize().toAbsolutePath();
        try {
            Files.createDirectories(storagePath);
            LOG.info("Local storage initialized at: {}", storagePath);
        } catch (IOException ex) {
            LOG.error("Failed to create local storage directory: {}", storagePath, ex);
        }
    }

    @Override
    public void upload(String key, InputStream inputStream, String contentType, long fileSize) {
        try {
            Path filePath = validatePath(key);
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Stored file locally: {}", filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file locally: " + key, ex);
        }
    }

    @Override
    public String generateAccessUrl(String key) {
        Path filePath = validatePath(key);
        return filePath.toUri().toString();
    }

    @Override
    public byte[] download(String key) {
        try {
            Path filePath = validatePath(key);
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file: " + key, ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path filePath = validatePath(key);
            Files.deleteIfExists(filePath);
            LOG.info("Deleted local file: {}", filePath);
        } catch (IOException ex) {
            LOG.warn("Failed to delete local file: {}", key, ex);
        }
    }

    /**
     * Resolves the given key against the storage base directory and verifies the result
     * does not escape the base via path traversal (e.g. {@code ../../etc/passwd}).
     *
     * @param key the storage key provided by the caller
     * @return the validated, normalized absolute path within the storage directory
     * @throws IllegalArgumentException if the resolved path escapes the base directory
     */
    private Path validatePath(String key) {
        Path resolved = canonicalBase.resolve(key).normalize().toAbsolutePath();
        if (!resolved.startsWith(canonicalBase)) {
            throw new IllegalArgumentException("Invalid storage key — path traversal detected: " + key);
        }
        return resolved;
    }
}
