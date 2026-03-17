package com.mindtrack.interview.service;

import com.mindtrack.interview.config.StorageProperties;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private S3StorageService service;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setBucketName("mindtrack-audio-test");
        properties.setPresignedUrlExpiryMinutes(15);
        service = new S3StorageService(s3Client, s3Presigner, properties);
    }

    @Test
    void shouldUploadFileToConfiguredBucket() {
        byte[] content = "s3-audio".getBytes(StandardCharsets.UTF_8);

        service.upload("interviews/1/audio.webm",
                new ByteArrayInputStream(content), "audio/webm", content.length);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertEquals("mindtrack-audio-test", request.bucket());
        assertEquals("interviews/1/audio.webm", request.key());
        assertEquals("audio/webm", request.contentType());
    }

    @Test
    void shouldGeneratePresignedAccessUrl() throws Exception {
        when(presignedGetObjectRequest.url())
                .thenReturn(URI.create("https://example.com/audio.webm?sig=123").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedGetObjectRequest);

        String url = service.generateAccessUrl("interviews/3/audio.webm");

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(requestCaptor.capture());
        GetObjectPresignRequest request = requestCaptor.getValue();
        assertEquals(Duration.ofMinutes(15), request.signatureDuration());
        assertEquals("mindtrack-audio-test", request.getObjectRequest().bucket());
        assertEquals("interviews/3/audio.webm", request.getObjectRequest().key());
        assertEquals("https://example.com/audio.webm?sig=123", url);
    }

    @Test
    void shouldDownloadBytesFromS3() {
        byte[] content = "downloaded".getBytes(StandardCharsets.UTF_8);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content));

        byte[] result = service.download("interviews/4/audio.webm");

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(requestCaptor.capture());
        assertEquals("mindtrack-audio-test", requestCaptor.getValue().bucket());
        assertEquals("interviews/4/audio.webm", requestCaptor.getValue().key());
        assertArrayEquals(content, result);
    }

    @Test
    void shouldDeleteObjectFromS3() {
        service.delete("interviews/5/audio.webm");

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        assertEquals("mindtrack-audio-test", requestCaptor.getValue().bucket());
        assertEquals("interviews/5/audio.webm", requestCaptor.getValue().key());
    }
}
