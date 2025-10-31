package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.exception.FileUploadException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucketName", "test-bucket");
    }

    @Test
    void uploadFile_shouldSucceed() throws Exception {
        // Given
        String objectName = "test-user/test-file.pdf";
        InputStream inputStream = InputStream.nullInputStream();
        long size = 12345L;
        String contentType = "application/pdf";

        // When
        minioService.uploadFile(objectName, inputStream, size, contentType);

        // Then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        PutObjectArgs capturedArgs = captor.getValue();
        assertThat(capturedArgs.bucket()).isEqualTo("test-bucket");
        assertThat(capturedArgs.object()).isEqualTo(objectName);
        assertThat(capturedArgs.stream()).isNotNull();
        assertThat(capturedArgs.contentType()).isEqualTo(contentType);
    }

    @Test
    void uploadFile_whenMinioClientFails_shouldThrowFileUploadException() throws Exception {
        // Given
        String objectName = "test-user/test-file.pdf";
        InputStream inputStream = InputStream.nullInputStream();
        long size = 12345L;
        String contentType = "application/pdf";

        doThrow(new RuntimeException("MinIO is down")).when(minioClient).putObject(any(PutObjectArgs.class));

        // When & Then
        assertThatThrownBy(() -> minioService.uploadFile(objectName, inputStream, size, contentType))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Error uploading file to MinIO");
    }
}
