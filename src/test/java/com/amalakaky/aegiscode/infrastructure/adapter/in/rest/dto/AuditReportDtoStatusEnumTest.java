package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditReportDtoStatusEnumTest {

    @Test
    void fromValue_shouldReturnMatchingEnum() {
        assertEquals(AuditReportDto.StatusEnum.IN_PROGRESS, AuditReportDto.StatusEnum.fromValue("IN_PROGRESS"));
        assertEquals(AuditReportDto.StatusEnum.COMPLETED, AuditReportDto.StatusEnum.fromValue("COMPLETED"));
        assertEquals(AuditReportDto.StatusEnum.FAILED, AuditReportDto.StatusEnum.fromValue("FAILED"));
    }

    @Test
    void fromValue_shouldThrowWhenValueUnknown() {
        assertThrows(IllegalArgumentException.class, () -> AuditReportDto.StatusEnum.fromValue("INVALID"));
    }

    @Test
    void getValue_shouldReturnUnderlyingValue() {
        assertEquals("IN_PROGRESS", AuditReportDto.StatusEnum.IN_PROGRESS.getValue());
        assertEquals("COMPLETED", AuditReportDto.StatusEnum.COMPLETED.getValue());
        assertEquals("FAILED", AuditReportDto.StatusEnum.FAILED.getValue());
    }

    @Test
    void toString_shouldReturnUnderlyingValue() {
        assertEquals("IN_PROGRESS", AuditReportDto.StatusEnum.IN_PROGRESS.toString());
        assertEquals("COMPLETED", AuditReportDto.StatusEnum.COMPLETED.toString());
        assertEquals("FAILED", AuditReportDto.StatusEnum.FAILED.toString());
    }
}
