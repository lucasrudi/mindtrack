package com.mindtrack.interview.service;

import java.io.InputStream;

/**
 * Abstraction for file storage operations.
 * Implementations provide S3 (production) or local file system (development) storage.
 */
public interface StorageService {

    /**
     * Uploads a file to storage.
     *
     * @param key         the storage key (path/filename)
     * @param inputStream the file content
     * @param contentType the MIME type of the file
     * @param fileSize    the size of the file in bytes
     */
    void upload(String key, InputStream inputStream, String contentType, long fileSize);

    /**
     * Generates a URL for accessing the stored file.
     * For S3, this is a presigned URL. For local storage, a file:// URL.
     *
     * @param key the storage key
     * @return the access URL
     */
    String generateAccessUrl(String key);

    /**
     * Deletes a file from storage.
     *
     * @param key the storage key
     */
    void delete(String key);
}
