CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    minio_path VARCHAR(255) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS document_tags (
    document_id UUID NOT NULL,
    tag VARCHAR(255) NOT NULL,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_document_name ON documents(document_name);
CREATE INDEX idx_document_tags_tag ON document_tags(tag);