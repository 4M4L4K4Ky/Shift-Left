CREATE TABLE users (
    id            VARCHAR2(36) PRIMARY KEY,
    username      VARCHAR2(100) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    role          VARCHAR2(50) NOT NULL,
    enabled       NUMBER(1) DEFAULT 1,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
