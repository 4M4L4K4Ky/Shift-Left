package com.amalakaky.aegiscode.infrastructure.adapter.out.pdf;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.AuditDetail;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.CweStat;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.GlobalReportData;
import com.amalakaky.aegiscode.application.port.out.ReportGeneratorPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa {@link ReportGeneratorPort} usando Apache PDFBox 3.x.
 * 
 * Genera un documento PDF estructurado con el informe global de la plataforma:
 * portada, resumen ejecutivo, top CWEs y detalle de auditorías recientes.
 */
@Component
public class PdfBoxReportAdapter implements ReportGeneratorPort {

  private static final Logger log = LoggerFactory.getLogger(PdfBoxReportAdapter.class);
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private static final float MARGIN = 50;
  private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
  private static final PDType1Font FONT_BOLD =
      new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDType1Font FONT_REG =
      new PDType1Font(Standard14Fonts.FontName.HELVETICA);
  private static final float Y_START = PDRectangle.A4.getHeight() - MARGIN;

  @Override
  public byte[] generateGlobalPdf(GlobalReportData data) {
    try (PDDocument document = new PDDocument()) {
      addCoverPage(document, data);
      addExecutiveSummary(document, data);
      addTopCwesPage(document, data);
      addRecentAuditsPage(document, data);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      document.save(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      log.error("Error generando PDF global", e);
      throw new IllegalStateException("Error al generar el informe PDF global", e);
    }
  }

  private void addCoverPage(PDDocument document, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
      float y = Y_START;
      writeLine(cs, FONT_BOLD, 24, MARGIN, y, "AegisCode");
      y -= 28;
      writeLine(cs, FONT_BOLD, 16, MARGIN, y, "Informe Global de Auditoria");
      y -= 22;
      writeLine(cs, FONT_REG, 10, MARGIN, y, "Shift-Left DevSecOps Platform");
      y -= 8;
      writeLine(cs, FONT_REG, 9, MARGIN, y, "Generado: " + LocalDateTime.now().format(DATE_FMT));
      y -= 40;
      drawLine(cs, MARGIN, y, PAGE_WIDTH - MARGIN, y);
      y -= 30;
      writeLine(cs, FONT_REG, 10, MARGIN, y,
          "Total de auditorias: " + data.totalAudits());
      y -= 16;
      writeLine(cs, FONT_REG, 10, MARGIN, y,
          "Total de vulnerabilidades: " + data.totalVulnerabilities());
    }
  }

  private void addExecutiveSummary(PDDocument document, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
      float y = Y_START;
      writeLine(cs, FONT_BOLD, 16, MARGIN, y, "Resumen Ejecutivo");
      y -= 30;
      drawLine(cs, MARGIN, y, PAGE_WIDTH - MARGIN, y);
      y -= 25;
      writeLine(cs, FONT_BOLD, 10, MARGIN, y, "Distribucion por Severidad:");
      y -= 18;
      for (Map.Entry<Integer, Long> entry : data.bySeverity().entrySet()) {
        writeLine(cs, FONT_REG, 10, MARGIN + 15, y,
            "Severidad " + entry.getKey() + ": " + entry.getValue() + " vulnerabilidades");
        y -= 14;
      }
    }
  }

  private void addTopCwesPage(PDDocument document, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    PDPageContentStream cs = new PDPageContentStream(document, page);
    float y = Y_START;
    writeLine(cs, FONT_BOLD, 16, MARGIN, y, "Top CWEs mas Frecuentes");
    y -= 30;
    drawLine(cs, MARGIN, y, PAGE_WIDTH - MARGIN, y);
    y -= 25;
    for (CweStat cwe : data.topCwes()) {
      if (y < MARGIN + 20) {
        cs.close();
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        cs = new PDPageContentStream(document, page);
        y = Y_START;
      }
      writeLine(cs, FONT_REG, 10, MARGIN, y,
          cwe.cweId() + " - " + cwe.count() + " ocurrencia(s)");
      y -= 14;
    }
    cs.close();
  }

  private void addRecentAuditsPage(PDDocument document, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    PDPageContentStream cs = new PDPageContentStream(document, page);
    float y = Y_START;
    writeLine(cs, FONT_BOLD, 16, MARGIN, y, "Auditorias Recientes");
    y -= 30;
    drawLine(cs, MARGIN, y, PAGE_WIDTH - MARGIN, y);
    y -= 25;
    for (AuditDetail audit : data.recentAudits()) {
      if (y < MARGIN + 60) {
        cs.close();
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        cs = new PDPageContentStream(document, page);
        y = Y_START;
      }
      writeLine(cs, FONT_BOLD, 10, MARGIN, y, "Scan: " + audit.scanId());
      y -= 14;
      writeLine(cs, FONT_REG, 9, MARGIN + 10, y,
          "Fecha: " + audit.date().format(DATE_FMT));
      y -= 12;
      writeLine(cs, FONT_REG, 9, MARGIN + 10, y,
          "Repo: " + (audit.repositoryUrl() != null ? audit.repositoryUrl() : "N/A"));
      y -= 12;
      writeLine(cs, FONT_REG, 9, MARGIN + 10, y,
          "Rama: " + (audit.branchName() != null ? audit.branchName() : "N/A"));
      y -= 20;
    }
    cs.close();
  }

  private static void writeLine(PDPageContentStream cs, PDType1Font font, int size,
      float x, float y, String text) throws IOException {
    cs.beginText();
    cs.setFont(font, size);
    cs.newLineAtOffset(x, y);
    cs.showText(text);
    cs.endText();
  }

  private static void drawLine(PDPageContentStream cs, float x1, float y1,
      float x2, float y2) throws IOException {
    cs.setLineWidth(0.5f);
    cs.moveTo(x1, y1);
    cs.lineTo(x2, y2);
    cs.stroke();
  }
}
