package com.amalakaky.aegiscode.application.port.out.ai;

import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.Optional;

public interface AuditorAgentPort {
  Optional<Vulnerability> analyze(String sourceCode);
}



