package com.amalakaky.aegiscode.application.port.out.ai;

public interface RemediationAgentPort {
    String generateCleanPatch(String vulnerableCode, String cweId);
}