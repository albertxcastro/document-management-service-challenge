package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioService minioService;

    @Transactional
    public void uploadDocument(UploadDocumentRequest request, MultipartFile file) throws IOException {
        String objectName = request.user() + "/" + request.name();

        minioService.uploadFile(
                objectName,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        Document document = new Document(
                UUID.randomUUID(),
                request.user(),
                request.name(),
                request.tags(),
                objectName,
                file.getSize(),
                file.getContentType(),
                Instant.now()
        );

        documentRepository.save(document);
    }
}
