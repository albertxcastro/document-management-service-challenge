package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadDocument(
            @Valid @RequestPart("metadata") UploadDocumentRequest request,
            @RequestPart("file") MultipartFile file) throws IOException {
        documentService.uploadDocument(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
