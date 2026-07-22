package com.amalakaky.aegiscode.infrastructure.adapter.out.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditRepository extends JpaRepository<AuditEntity, String> {
}