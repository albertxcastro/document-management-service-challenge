package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void uploadDocument_shouldCallServicesAndSaveDocument() throws IOException {
        // Given
        UploadDocumentRequest request = new UploadDocumentRequest("test-user", "test-doc.pdf", List.of("tag1", "tag2"));
        MockMultipartFile file = new MockMultipartFile("file", "test-doc.pdf", "application/pdf", "test content".getBytes());

        // When
        documentService.uploadDocument(request, file);

        // Then
        // Verify MinioService call
        ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<Long> sizeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);

        verify(minioService).uploadFile(objectNameCaptor.capture(), inputStreamCaptor.capture(), sizeCaptor.capture(), contentTypeCaptor.capture());

        assertThat(objectNameCaptor.getValue()).isEqualTo("test-user/test-doc.pdf");
        assertThat(sizeCaptor.getValue()).isEqualTo(file.getSize());
        assertThat(contentTypeCaptor.getValue()).isEqualTo(file.getContentType());

        // Verify DocumentRepository call
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());

        Document capturedDocument = documentCaptor.getValue();
        assertThat(capturedDocument.id()).isNotNull();
        assertThat(capturedDocument.userId()).isEqualTo(request.user());
        assertThat(capturedDocument.documentName()).isEqualTo(request.name());
        assertThat(capturedDocument.tags()).isEqualTo(request.tags());
        assertThat(capturedDocument.minioPath()).isEqualTo("test-user/test-doc.pdf");
        assertThat(capturedDocument.fileSize()).isEqualTo(file.getSize());
        assertThat(capturedDocument.fileType()).isEqualTo(file.getContentType());
        assertThat(capturedDocument.createdAt()).isNotNull();
    }

    @Test
    void uploadDocument_whenRepositoryFails_shouldThrowException() throws IOException {
        // Given
        UploadDocumentRequest request = new UploadDocumentRequest("test-user", "test-doc.pdf", List.of("tag1"));
        MockMultipartFile file = new MockMultipartFile("file", "test-doc.pdf", "application/pdf", "test content".getBytes());

        // Configure repository to throw an exception
        doThrow(new RuntimeException("Database is down")).when(documentRepository).save(any(Document.class));

        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(request, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database is down");
    }
}
