CREATE TABLE audit_reports (
                               scan_id VARCHAR2(36) PRIMARY KEY,
                               status VARCHAR2(50) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               repository_url VARCHAR2(512),
                               branch_name VARCHAR2(100)
);