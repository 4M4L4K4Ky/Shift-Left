package com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository;

import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.AuditReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataAuditRepository extends JpaRepository<AuditReportEntity, String> {
}