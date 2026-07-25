package com.amalakaky.aegiscode.application.port.out.ai;

import com.amalakaky.aegiscode.domain.model.Vulnerability;
import java.util.List;

public interface RepositoryScannerPort {
  List<Vulnerability> scanRepository(String sourceCodePayload);
}



