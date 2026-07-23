CREATE TABLE IF NOT EXISTS audit_reports (
    scan_id VARCHAR(36) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    repository_url VARCHAR(512),
    branch_name VARCHAR(100)
    );