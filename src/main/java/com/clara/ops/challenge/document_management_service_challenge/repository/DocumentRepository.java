package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void save(Document document) {
        String sql = "INSERT INTO documents (id, user_id, document_name, minio_path, file_size, file_type, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, document.id(), document.userId(), document.documentName(), document.minioPath(), document.fileSize(), document.fileType(), document.createdAt());

        String tagSql = "INSERT INTO document_tags (document_id, tag) VALUES (?, ?)";
        document.tags().forEach(tag -> jdbcTemplate.update(tagSql, document.id(), tag));
    }
}
