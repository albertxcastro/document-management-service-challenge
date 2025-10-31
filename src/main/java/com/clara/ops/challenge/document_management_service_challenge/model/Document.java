package com.clara.ops.challenge.document_management_service_challenge.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Document(
    UUID id,
    String userId,
    String documentName,
    List<String> tags,
    String minioPath,
    long fileSize,
    String fileType,
    Instant createdAt
) {}
