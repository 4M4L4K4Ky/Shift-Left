package com.amalakaky.aegiscode.infrastructure.adapter.out.pdf;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.AuditDetail;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.CweStat;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.GlobalReportData;
import com.amalakaky.aegiscode.application.port.out.ReportGeneratorPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxReportAdapter implements ReportGeneratorPort {

  private static final Logger log = LoggerFactory.getLogger(PdfBoxReportAdapter.class);
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private static final float MARGIN = 50;
  private static final float PAGE_W = PDRectangle.A4.getWidth();
  private static final float PAGE_H = PDRectangle.A4.getHeight();
  private static final PDType1Font BOLD =
      new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDType1Font REG =
      new PDType1Font(Standard14Fonts.FontName.HELVETICA);

  private static final Map<String, String> CWE_DESC = new LinkedHashMap<>();

  static {
    CWE_DESC.put("CWE-89", "Inyeccion SQL - Datos no validados en consultas SQL");
    CWE_DESC.put("CWE-79", "XSS - Scripting entre sitios en salida HTML");
    CWE_DESC.put("CWE-78", "Inyeccion de comandos OS");
    CWE_DESC.put("CWE-22", "Path Traversal - Acceso a archivos fuera del directorio");
    CWE_DESC.put("CWE-327", "Algoritmo criptografico roto o inseguro");
    CWE_DESC.put("CWE-798", "Credenciales hardcodeadas en el codigo fuente");
    CWE_DESC.put("CWE-287", "Autenticacion incorrecta o inexistente");
    CWE_DESC.put("CWE-862", "Autorizacion omitida en acceso a recursos");
    CWE_DESC.put("CWE-200", "Exposicion de informacion sensible");
    CWE_DESC.put("CWE-502", "Deserializacion insegura de datos");
    CWE_DESC.put("CWE-611", "XXE - Procesamiento XML externo malicioso");
    CWE_DESC.put("CWE-918", "SSRF - Peticiones del lado del servidor forjadas");
    CWE_DESC.put("CWE-352", "CSRF - Falsificacion de peticion en sitios cruzados");
    CWE_DESC.put("CWE-190", "Desbordamiento de entero sin validar");
    CWE_DESC.put("CWE-400", "Consumo incontrolado de recursos");
    CWE_DESC.put("CWE-20", "Validacion de entrada incorrecta");
    CWE_DESC.put("CWE-116", "Codificacion de salida incorrecta");
    CWE_DESC.put("CWE-276", "Permisos por defecto incorrectos");
    CWE_DESC.put("CWE-522", "Gestion de credenciales insuficiente");
    CWE_DESC.put("CWE-295", "Validacion de certificado incorrecta");
    CWE_DESC.put("CWE-310", "Problemas criptograficos");
    CWE_DESC.put("CWE-362", "Condicion de carrera");
    CWE_DESC.put("CWE-476", "Desreferencia de puntero nulo");
    CWE_DESC.put("CWE-494", "Descarga de codigo sin integridad");
    CWE_DESC.put("CWE-532", "Insercion de informacion sensible en logs");
    CWE_DESC.put("CWE-601", "Redireccion a URL no validada");
    CWE_DESC.put("CWE-754", "Comprobacion de condiciones incorrecta");
    CWE_DESC.put("CWE-770", "Asignacion de recursos sin limites");
    CWE_DESC.put("CWE-834", "Iteracion excesiva en un bucle");
    CWE_DESC.put("CWE-1048", "Codigo con licencia incompatible");
  }

  @Override
  public byte[] generateGlobalPdf(GlobalReportData data) {
    try (PDDocument doc = new PDDocument()) {
      addCoverPage(doc, data);
      addIntroductionPage(doc);
      addSummaryPage(doc, data);
      addCwesPage(doc, data);
      addAuditsPage(doc, data);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      doc.save(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      log.error("Error generando PDF global", e);
      throw new IllegalStateException("Error al generar el informe PDF global", e);
    }
  }

  private void addCoverPage(PDDocument doc, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    doc.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
      cs.setNonStrokingColor(0.12f, 0.12f, 0.12f);
      cs.addRect(0, PAGE_H - 70, PAGE_W, 70);
      cs.fill();
      cs.setNonStrokingColor(1, 1, 1);
      writeLine(cs, BOLD, 26, MARGIN, PAGE_H - 48, "AegisCode");

      cs.setNonStrokingColor(0, 0, 0);
      float y = PAGE_H - 105;
      writeLine(cs, BOLD, 14, MARGIN, y, "Informe Global de Seguridad");
      y -= 20;
      writeLine(cs, REG, 9, MARGIN, y, "Generado: " + LocalDateTime.now().format(DATE_FMT));
      y -= 8;
      writeLine(cs, REG, 9, MARGIN, y, "Shift-Left DevSecOps Platform");
      y -= 25;
      drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
      y -= 22;
      writeMetric(cs, y, "Auditorias realizadas", String.valueOf(data.totalAudits()));
      y -= 16;
      writeMetric(cs, y, "Vulnerabilidades encontradas", String.valueOf(data.totalVulnerabilities()));
      y -= 16;
      writeMetric(cs, y, "Tipos de CWE detectados", String.valueOf(data.topCwes().size()));
    }
  }

  private void addIntroductionPage(PDDocument doc) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    doc.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
      float y = PAGE_H - MARGIN;
      writeLine(cs, BOLD, 16, MARGIN, y, "Que es AegisCode?");
      y -= 26;
      drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
      y -= 22;

      writeLine(cs, REG, 10, MARGIN, y,
          "AegisCode es una plataforma de seguridad de codigo que adopta un enfoque");
      y -= 14;
      writeLine(cs, REG, 10, MARGIN, y,
          "Shift-Left DevSecOps para detectar vulnerabilidades en etapas tempranas");
      y -= 14;
      writeLine(cs, REG, 10, MARGIN, y,
          "del desarrollo. Integra agentes de inteligencia artificial especializados");
      y -= 14;
      writeLine(cs, REG, 10, MARGIN, y,
          "que analizan codigo fuente y repositorios completos en busca de fallos de");
      y -= 14;
      writeLine(cs, REG, 10, MARGIN, y,
          "seguridad OWASP Top 10 y CWEs comunes.");
      y -= 22;

      writeLine(cs, REG, 10, MARGIN, y,
          "El proceso de escaneo se compone de tres etapas principales:");
      y -= 20;

      writeLine(cs, BOLD, 10, MARGIN + 10, y, "1. Scanner Agent");
      y -= 14;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "Descompone el codigo fuente, identifica las estructuras del programa y");
      y -= 12;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "prepara el contexto para el analisis de seguridad.");
      y -= 20;

      writeLine(cs, BOLD, 10, MARGIN + 10, y, "2. Auditor Agent");
      y -= 14;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "Aplica modelos de IA entrenados en patrones de vulnerabilidades para");
      y -= 12;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "detectar fallos como inyeccion SQL, XSS, credenciales hardcodeadas,");
      y -= 12;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "path traversal, y otros CWEs del OWASP Top 10.");
      y -= 20;

      writeLine(cs, BOLD, 10, MARGIN + 10, y, "3. Remediation Agent");
      y -= 14;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "Genera parches seguros especificos para cada vulnerabilidad encontrada,");
      y -= 12;
      writeLine(cs, REG, 9, MARGIN + 20, y,
          "proporcionando al desarrollador el codigo corregido listo para aplicar.");
    }
  }

  private void addSummaryPage(PDDocument doc, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    doc.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
      float y = PAGE_H - MARGIN;
      writeLine(cs, BOLD, 14, MARGIN, y, "Resumen Ejecutivo");
      y -= 22;
      drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
      y -= 18;

      writeLine(cs, REG, 9, MARGIN, y,
          "AegisCode analiza codigo fuente utilizando agentes de IA especializados");
      y -= 13;
      writeLine(cs, REG, 9, MARGIN, y,
          "para detectar vulnerabilidades de seguridad en etapas tempranas del desarrollo.");
      y -= 22;

      writeLine(cs, BOLD, 10, MARGIN, y, "Distribucion por Severidad");
      y -= 16;
      y = drawSeverityTable(cs, data.bySeverity(), y);

      y -= 20;
      writeLine(cs, BOLD, 10, MARGIN, y, "Grafico de Severidad");
      y -= 14;
      long total = data.bySeverity().values().stream().mapToLong(Long::longValue).sum();
      y = drawDonutChart(cs, data.bySeverity(), total, y);
      y -= 18;

      writeLine(cs, BOLD, 10, MARGIN, y, "Metricas Globales");
      y -= 16;
      writeMetric(cs, y, "Total auditorias", String.valueOf(data.totalAudits()));
      y -= 14;
      writeMetric(cs, y, "Total vulnerabilidades", String.valueOf(data.totalVulnerabilities()));
      y -= 14;
      writeMetric(cs, y, "Promedio por auditoria",
          String.format("%.1f", data.totalAudits() > 0
              ? (double) data.totalVulnerabilities() / data.totalAudits() : 0));
    }
  }

  private float drawSeverityTable(PDPageContentStream cs, Map<Integer, Long> bySeverity,
      float startY) throws IOException {
    float y = startY;
    long maxVal = bySeverity.values().stream().mapToLong(Long::longValue).max().orElse(1);
    float tableW = PAGE_W - MARGIN * 2;
    float rowH = 18;

    cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
    cs.addRect(MARGIN, y - rowH + 2, tableW, rowH);
    cs.fill();
    cs.setNonStrokingColor(0, 0, 0);
    writeLine(cs, BOLD, 9, MARGIN + 6, y - 5, "Severidad");
    writeLine(cs, BOLD, 9, MARGIN + 80, y - 5, "Cantidad");
    writeLine(cs, BOLD, 9, MARGIN + 145, y - 5, "%");
    writeLine(cs, BOLD, 9, MARGIN + 180, y - 5, "Barra");
    y -= (rowH + 6);

    for (Map.Entry<Integer, Long> e : bySeverity.entrySet()) {
      int sev = e.getKey();
      long count = e.getValue();
      float pct = maxVal > 0 ? (float) count / maxVal * 100 : 0;

      writeLine(cs, BOLD, 9, MARGIN + 6, y - 5, severityLabel(sev));
      writeLine(cs, REG, 9, MARGIN + 80, y - 5, String.valueOf(count));
      writeLine(cs, REG, 9, MARGIN + 145, y - 5, String.format("%.0f%%", pct));

      float barMax = PAGE_W - MARGIN - 180 - MARGIN - 5;
      float barW = maxVal > 0 ? (count * barMax) / maxVal : 0;
      float r = sev >= 9 ? 0.75f : sev >= 7 ? 0.55f : sev >= 4 ? 0.35f : 0.2f;
      cs.setNonStrokingColor(r, 0.15f, 0.15f);
      cs.addRect(MARGIN + 180, y - rowH + 4, barW, rowH - 8);
      cs.fill();
      cs.setNonStrokingColor(0, 0, 0);

      y -= (rowH + 2);
    }
    drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
    return y;
  }

  private float drawDonutChart(PDPageContentStream cs, Map<Integer, Long> bySeverity,
      long total, float startY) throws IOException {
    if (total == 0) {
      return startY;
    }

    float cx = PAGE_W / 2;
    float cy = startY - 45;
    float outerR = 42;
    float innerR = 22;
    float legendX = cx + outerR + 25;
    float legendY = cy + 35;

    int[] severities = {9, 7, 4, 1, 0};
    String[] labels = {"Critico", "Alto", "Medio", "Bajo", "Info"};
    float[][] colors = {
        {0.8f, 0.15f, 0.15f},
        {0.85f, 0.45f, 0.1f},
        {0.75f, 0.7f, 0.1f},
        {0.2f, 0.7f, 0.2f},
        {0.1f, 0.55f, 0.15f}
    };

    float angleStart = 90;

    cs.setLineWidth(1);

    for (int i = 0; i < severities.length; i++) {
      int sev = severities[i];
      Long countObj = bySeverity.get(sev);
      long count = countObj != null ? countObj : 0;
      if (count == 0) {
        continue;
      }

      float sweep = (float) count / total * 360;

      cs.setNonStrokingColor(colors[i][0], colors[i][1], colors[i][2]);
      drawDonutWedge(cs, cx, cy, outerR, innerR, angleStart, sweep);

      float midAngle = (float) Math.toRadians(angleStart - sweep / 2);
      float lx = cx + (outerR + 8) * (float) Math.cos(midAngle);
      float ly = cy - (outerR + 8) * (float) Math.sin(midAngle);
      float pct = (float) count / total * 100;
      cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
      writeLine(cs, REG, 7, lx - 8, ly - 3, String.format("%.0f%%", pct));

      angleStart -= sweep;
    }

    cs.setNonStrokingColor(0, 0, 0);
    for (int i = 0; i < severities.length; i++) {
      int sev = severities[i];
      Long countObj = bySeverity.get(sev);
      long count = countObj != null ? countObj : 0;
      if (count == 0) {
        continue;
      }
      cs.setNonStrokingColor(colors[i][0], colors[i][1], colors[i][2]);
      cs.addRect(legendX, legendY - i * 14, 10, 10);
      cs.fill();
      cs.setNonStrokingColor(0, 0, 0);
      writeLine(cs, REG, 8, legendX + 14, legendY - i * 14 + 1,
          labels[i] + " (" + count + ")");
    }

    return cy - outerR - 15;
  }

  private void drawDonutWedge(PDPageContentStream cs, float cx, float cy,
      float outerR, float innerR, float startAngle, float sweep) throws IOException {
    if (sweep <= 0) {
      return;
    }
    int segments = Math.max(4, (int) (sweep / 5));

    float start = (float) Math.toRadians(startAngle);
    float end = (float) Math.toRadians(startAngle - sweep);

    for (int i = 0; i < segments; i++) {
      float a1 = start + (end - start) * i / segments;
      float a2 = start + (end - start) * (i + 1) / segments;

      float x1o = cx + outerR * (float) Math.cos(a1);
      float y1o = cy - outerR * (float) Math.sin(a1);
      float x2o = cx + outerR * (float) Math.cos(a2);
      float y2o = cy - outerR * (float) Math.sin(a2);
      float x1i = cx + innerR * (float) Math.cos(a1);
      float y1i = cy - innerR * (float) Math.sin(a1);
      float x2i = cx + innerR * (float) Math.cos(a2);
      float y2i = cy - innerR * (float) Math.sin(a2);

      cs.moveTo(x1o, y1o);
      cs.lineTo(x2o, y2o);
      cs.lineTo(x2i, y2i);
      cs.lineTo(x1i, y1i);
      cs.closePath();
      cs.fill();
    }
  }

  private static String severityLabel(int sev) {
    if (sev >= 9) return "Critico";
    if (sev >= 7) return "Alto";
    if (sev >= 4) return "Medio";
    if (sev >= 1) return "Bajo";
    return "Info";
  }

  private void addCwesPage(PDDocument doc, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    doc.addPage(page);
    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
      float y = PAGE_H - MARGIN;
      writeLine(cs, BOLD, 14, MARGIN, y, "Top CWEs mas Frecuentes");
      y -= 22;
      drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
      y -= 16;

      float col0 = MARGIN;
      float col1 = col0 + 55;
      float col2 = col1 + 250;
      float tableW = PAGE_W - MARGIN * 2;

      cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
      cs.addRect(MARGIN, y - 16, tableW, 16);
      cs.fill();
      cs.setNonStrokingColor(0, 0, 0);
      writeLine(cs, BOLD, 9, col0 + 4, y - 5, "CWE");
      writeLine(cs, BOLD, 9, col1 + 4, y - 5, "Descripcion");
      writeLine(cs, BOLD, 9, col2 + 4, y - 5, "Total");
      y -= 22;

      for (CweStat cwe : data.topCwes()) {
        if (y < MARGIN + 20) {
          continue;
        }
        writeLine(cs, BOLD, 9, col0 + 4, y - 4, cwe.cweId());
        String desc = CWE_DESC.getOrDefault(cwe.cweId(), "Sin clasificacion especifica");
        writeTruncated(cs, REG, 8, col1 + 4, y - 4, 240, desc);
        writeLine(cs, BOLD, 9, col2 + 4, y - 4, String.valueOf(cwe.count()));
        y -= 16;
      }
      drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
    }
  }

  private void addAuditsPage(PDDocument doc, GlobalReportData data) throws IOException {
    PDPage page = new PDPage(PDRectangle.A4);
    doc.addPage(page);
    PDPageContentStream cs = new PDPageContentStream(doc, page);
    float y = PAGE_H - MARGIN;
    writeLine(cs, BOLD, 14, MARGIN, y, "Auditorias Recientes");
    y -= 22;
    drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
    y -= 16;

    float scanW = 155;
    float dateW = 95;
    float repoW = PAGE_W - MARGIN * 2 - scanW - dateW - 8;
    float[] col = {MARGIN, MARGIN + scanW, MARGIN + scanW + dateW};
    float tableW = PAGE_W - MARGIN * 2;

    cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
    cs.addRect(MARGIN, y - 16, tableW, 16);
    cs.fill();
    cs.setNonStrokingColor(0, 0, 0);
    writeLine(cs, BOLD, 9, col[0] + 4, y - 5, "Scan ID");
    writeLine(cs, BOLD, 9, col[1] + 4, y - 5, "Fecha");
    writeLine(cs, BOLD, 9, col[2] + 4, y - 5, "Repositorio");
    y -= 22;

    List<AuditLink> links = new ArrayList<>();

    for (AuditDetail a : data.recentAudits()) {
      if (y < MARGIN + 20) {
        continue;
      }
      writeTruncated(cs, REG, 8, col[0] + 4, y - 4, scanW - 5, a.scanId());
      writeLine(cs, REG, 8, col[1] + 4, y - 4, a.date().format(DATE_FMT));

      String repo = a.repositoryUrl() != null ? a.repositoryUrl() : "N/A";
      if (!"N/A".equals(repo)) {
        cs.setNonStrokingColor(0, 0, 0.6f);
        writeTruncated(cs, REG, 8, col[2] + 4, y - 4, repoW - 5, repo);
        cs.setNonStrokingColor(0, 0, 0);
        links.add(new AuditLink(repo, col[2] + 4, y - 17, repoW - 5, 15));
      } else {
        writeTruncated(cs, REG, 8, col[2] + 4, y - 4, repoW - 5, "N/A");
      }
      y -= 16;
    }
    drawLine(cs, MARGIN, y, PAGE_W - MARGIN, y);
    cs.close();

    for (AuditLink al : links) {
      PDAnnotationLink link = new PDAnnotationLink();
      PDActionURI uri = new PDActionURI();
      uri.setURI(al.url);
      link.setAction(uri);
      PDRectangle rect = new PDRectangle();
      rect.setLowerLeftX(al.x);
      rect.setLowerLeftY(al.y);
      rect.setUpperRightX(al.x + al.w);
      rect.setUpperRightY(al.y + al.h);
      link.setRectangle(rect);
      page.getAnnotations().add(link);
    }
  }

  private record AuditLink(String url, float x, float y, float w, float h) {
  }

  private void writeLine(PDPageContentStream cs, PDType1Font font, int size,
      float x, float y, String text) throws IOException {
    cs.beginText();
    cs.setFont(font, size);
    cs.newLineAtOffset(x, y);
    cs.showText(text);
    cs.endText();
  }

  private void writeTruncated(PDPageContentStream cs, PDType1Font font, int size,
      float x, float y, float maxW, String text) throws IOException {
    String d = text;
    float tw = (font.getStringWidth(d) / 1000f) * size;
    if (tw > maxW) {
      while (tw > maxW && d.length() > 3) {
        d = d.substring(0, d.length() - 1);
        tw = (font.getStringWidth(d + "...") / 1000f) * size;
      }
      d += "...";
    }
    cs.beginText();
    cs.setFont(font, size);
    cs.newLineAtOffset(x, y);
    cs.showText(d);
    cs.endText();
  }

  private void writeMetric(PDPageContentStream cs, float y, String label, String value)
      throws IOException {
    writeLine(cs, REG, 9, MARGIN + 5, y, label + ":");
    writeLine(cs, BOLD, 9, MARGIN + 135, y, value);
  }

  private void drawLine(PDPageContentStream cs, float x1, float y1,
      float x2, float y2) throws IOException {
    cs.setLineWidth(0.5f);
    cs.moveTo(x1, y1);
    cs.lineTo(x2, y2);
    cs.stroke();
  }
}
