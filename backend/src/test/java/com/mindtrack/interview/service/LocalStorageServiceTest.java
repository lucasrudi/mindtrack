package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setLocalStoragePath(tempDir.resolve("audio").toString());
        service = new LocalStorageService(properties);
    }

    @Test
    void shouldStoreDownloadAndExposeLocalFile() {
        byte[] content = "mindtrack-audio".getBytes(StandardCharsets.UTF_8);

        service.upload("interviews/1/audio.webm",
                new ByteArrayInputStream(content), "audio/webm", content.length);

        assertArrayEquals(content, service.download("interviews/1/audio.webm"));
        assertTrue(service.generateAccessUrl("interviews/1/audio.webm").startsWith("file:"));
    }

    @Test
    void shouldDeleteStoredFile() throws Exception {
        byte[] content = "to-delete".getBytes(StandardCharsets.UTF_8);
        service.upload("interviews/2/audio.webm",
                new ByteArrayInputStream(content), "audio/webm", content.length);

        Path storedPath = tempDir.resolve("audio/interviews/2/audio.webm");
        assertTrue(Files.exists(storedPath));

        service.delete("interviews/2/audio.webm");

        assertFalse(Files.exists(storedPath));
    }

    @Test
    void shouldRejectPathTraversalKeys() {
        byte[] content = "escape".getBytes(StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> service.upload(
                "../escape.webm",
                new ByteArrayInputStream(content),
                "audio/webm",
                content.length));
        assertThrows(IllegalArgumentException.class, () -> service.generateAccessUrl("../escape.webm"));
        assertThrows(IllegalArgumentException.class, () -> service.download("../escape.webm"));
    }
}
