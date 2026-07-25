package com.amalakaky.aegiscode.application.port.usecase;

import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para consultar estadísticas agregadas de vulnerabilidades.
 * <p>
 * Delega en el puerto de salida {@link AuditStatisticsRepositoryPort}
 * que consulta la vista de base de datos {@code view_vulnerability_severity_stats}.
 */
@Service
public class GetAuditStatisticsUseCaseImpl implements GetAuditStatisticsUseCase {

  private final AuditStatisticsRepositoryPort statisticsRepositoryPort;

  public GetAuditStatisticsUseCaseImpl(AuditStatisticsRepositoryPort statisticsRepositoryPort) {
    this.statisticsRepositoryPort = statisticsRepositoryPort;
  }

  @Override
  public Map<Integer, Long> getSeverityStatistics() {
    return statisticsRepositoryPort.getVulnerabilitiesBySeverity();
  }
}



