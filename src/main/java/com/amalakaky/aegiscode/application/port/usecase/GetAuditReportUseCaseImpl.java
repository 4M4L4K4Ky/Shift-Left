package com.amalakaky.aegiscode.application.port.usecase;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase;
import com.amalakaky.aegiscode.application.port.in.GetAuditStatisticsUseCase;
import com.amalakaky.aegiscode.application.port.out.ReportGeneratorPort;
import com.amalakaky.aegiscode.application.port.out.db.AuditStatisticsRepositoryPort;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para generar el informe PDF global de la plataforma.
 * <p>
 * Obtiene las estadísticas del dashboard desde {@link GetAuditStatisticsUseCase},
 * complementa con el detalle de vulnerabilidades de las auditorías recientes
 * desde {@link AuditStatisticsRepositoryPort}, y delega la generación del PDF
 * en {@link ReportGeneratorPort}.
 */
@Service
public class GetAuditReportUseCaseImpl implements GetAuditReportUseCase {

  private static final Logger log = LoggerFactory.getLogger(GetAuditReportUseCaseImpl.class);

  private final GetAuditStatisticsUseCase statsUseCase;
  private final AuditStatisticsRepositoryPort statsPort;
  private final ReportGeneratorPort reportGenerator;

  public GetAuditReportUseCaseImpl(GetAuditStatisticsUseCase statsUseCase,
      AuditStatisticsRepositoryPort statsPort,
      ReportGeneratorPort reportGenerator) {
    this.statsUseCase = statsUseCase;
    this.statsPort = statsPort;
    this.reportGenerator = reportGenerator;
  }

  @Override
  public byte[] getGlobalReport() {
    var dashboard = statsUseCase.getDashboardStats();

    List<CweStat> topCwes = dashboard.byCwe().stream()
        .map(c -> new CweStat(c.cweId(), c.count()))
        .collect(Collectors.toList());

    List<AuditDetail> recentAudits = statsPort.findRecentAudits(10).stream()
        .map(a -> new AuditDetail(
            a.scanId(),
            a.date(),
            a.repositoryUrl(),
            a.branchName(),
            List.of()
        ))
        .collect(Collectors.toList());

    var data = new GlobalReportData(
        dashboard.totalAudits(),
        dashboard.totalVulnerabilities(),
        dashboard.bySeverity(),
        topCwes,
        recentAudits
    );

    log.info("Generando informe PDF global: {} auditorias, {} vulnerabilidades",
        data.totalAudits(), data.totalVulnerabilities());
    return reportGenerator.generateGlobalPdf(data);
  }
}
