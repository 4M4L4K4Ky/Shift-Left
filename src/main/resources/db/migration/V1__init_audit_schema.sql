CREATE TABLE audit_reports (
                               scan_id VARCHAR(36) PRIMARY KEY,
                               status VARCHAR(50) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);