package com.clara.ops.challenge.document_management_service_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UploadDocumentRequest(
    @NotBlank(message = "User cannot be blank")
    String user,
    @NotBlank(message = "Document name cannot be blank")
    String name,
    @NotEmpty(message = "Tags cannot be empty")
    List<String> tags
) {}
