package com.amalakaky.aegiscode.infrastructure.adapter.out.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.AuditDetail;
import com.amalakaky.aegiscode.application.port.in.GetAuditReportUseCase.GlobalReportData;
import com.amalakaky.aegiscode.domain.model.CweCount;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class PdfBoxReportAdapterTest {

  private final PdfBoxReportAdapter adapter = new PdfBoxReportAdapter();

  @Test
  void generateGlobalPdf_shouldProduceValidPdfWithAllData() throws IOException {
    var data = new GlobalReportData(
        10L,
        100L,
        Map.of(9, 20L, 7, 30L, 4, 25L, 1, 15L, 0, 10L),
        List.of(
            new CweCount("CWE-89", 30L),
            new CweCount("CWE-79", 20L)
        ),
        List.of(
            new AuditDetail("scan-001", LocalDateTime.of(2026, 7, 25, 10, 0),
                "https://github.com/amalakaky/demo.git", "main", List.of()),
            new AuditDetail("INLINE-SOURCE", LocalDateTime.of(2026, 7, 24, 15, 30),
                "INLINE_SOURCE_SNIPPET", "DIRECT_PAYLOAD", List.of())
        )
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(5);
    }
  }

  @Test
  void generateGlobalPdf_shouldHandleEmptyData() throws IOException {
    var data = new GlobalReportData(
        0L, 0L, Map.of(), List.of(), List.of()
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(3);
    }
  }

  @Test
  void generateGlobalPdf_shouldHandleSingleSeverity() throws IOException {
    var data = new GlobalReportData(
        1L, 5L, Map.of(9, 5L), List.of(), List.of()
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(3);
    }
  }

  @Test
  void generateGlobalPdf_withLongRepoUrl_shouldTruncateText() throws IOException {
    var data = new GlobalReportData(
        2L, 0L, Map.of(9, 1L),
        List.of(),
        List.of(
            new AuditDetail("scan-trunc", LocalDateTime.of(2026, 7, 25, 10, 0),
                "https://github.com/amalakaky/very-long-repository-name-that-exceeds-the-available-column-width-and-forces-truncation-in-the-pdf-report-generator.git",
                "main", List.of())
        )
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(4);
    }
  }

  @Test
  void generateGlobalPdf_withManyCwes_shouldHandlePageOverflow() throws IOException {
    var manyCwes = IntStream.range(0, 50)
        .mapToObj(i -> new CweCount("CWE-" + (89 - i), 1L))
        .toList();
    var data = new GlobalReportData(
        2L, 50L, Map.of(9, 50L), manyCwes, List.of()
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(4);
    }
  }

  @Test
  void generateGlobalPdf_withManyAudits_shouldHandlePageOverflowAndLinkAnnotations()
      throws IOException {
    var manyAudits = IntStream.range(0, 55)
        .mapToObj(i -> {
          String repo = i < 10
              ? "http://example.com/repo" + i + ".git"
              : i < 30
                  ? "https://github.com/test/repo" + i + ".git"
                  : i < 50
                      ? "INLINE_SOURCE_SNIPPET"
                      : null;
          return new AuditDetail("scan-" + i, LocalDateTime.of(2026, 7, 25, 10, 0),
              repo, "main", List.of());
        })
        .toList();
    var data = new GlobalReportData(
        55L, 0L, Map.of(9, 0L), List.of(),
        manyAudits
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(4);
      PDPage lastPage = doc.getPage(doc.getNumberOfPages() - 1);
      assertThat(lastPage.getAnnotations()).isNotEmpty();
    }
  }

  @Test
  void generateGlobalPdf_withMultipleSegments_shouldDrawDonutWedges() throws IOException {
    var data = new GlobalReportData(
        5L, 50L, Map.of(9, 15L, 7, 15L, 4, 10L, 1, 5L, 0, 5L),
        List.of(), List.of()
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(3);
    }
  }

  @Test
  void generateGlobalPdf_shouldThrowIllegalStateExceptionWhenSaveFails() {
    try (MockedConstruction<PDDocument> docMock = mockConstruction(PDDocument.class,
        (mock, context) -> doThrow(new IOException("Disk full"))
            .when(mock).save(any(OutputStream.class)));
         MockedConstruction<PDPageContentStream> csMock = mockConstruction(
             PDPageContentStream.class)) {
      var data = new GlobalReportData(0L, 0L, Map.of(), List.of(), List.of());

      assertThatThrownBy(() -> adapter.generateGlobalPdf(data))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Error al generar el informe PDF global")
          .hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  void generateGlobalPdf_withAudits_shouldCoverAllBranchesAndOverflow() throws IOException {
    List<AuditDetail> audits = new ArrayList<>();

    // 1. Rama http:// (primera condición del 'if' evaluada a true)
    audits.add(new AuditDetail(
        "scan-http", LocalDateTime.of(2026, 7, 25, 10, 0),
        "http://example.com/repo-http.git", "main", List.of()
    ));

    // 2. Rama https:// (primera condición false, segunda true)
    audits.add(new AuditDetail(
        "scan-https", LocalDateTime.of(2026, 7, 25, 10, 0),
        "https://github.com/example/repo-https.git", "main", List.of()
    ));

    // 3. Rama sin http/https (ambas condiciones false -> bloque else)
    audits.add(new AuditDetail(
        "scan-inline", LocalDateTime.of(2026, 7, 25, 10, 0),
        "INLINE_SOURCE_SNIPPET", "main", List.of()
    ));

    // 4. Rama repositoryUrl == null (evalúa repositoryUrl() != null a false -> repo = "N/A")
    audits.add(new AuditDetail(
        "scan-null", LocalDateTime.of(2026, 7, 25, 10, 0),
        null, "main", List.of()
    ));

        // 5. Overflow de elementos (> 42 auditorías) para forzar (y < MARGIN + 20)
        // y ejecutar el 'continue'
    for (int i = 0; i < 50; i++) {
      audits.add(new AuditDetail(
          "scan-overflow-" + i, LocalDateTime.of(2026, 7, 25, 10, 0),
          "https://github.com/example/overflow-" + i + ".git", "main", List.of()
      ));
    }

    var data = new GlobalReportData(
        54L, 0L, Map.of(9, 1L), List.of(), audits
    );

    byte[] pdfBytes = adapter.generateGlobalPdf(data);

    assertThat(pdfBytes).isNotEmpty();

    try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
      assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(4);
      PDPage lastPage = doc.getPage(doc.getNumberOfPages() - 1);
      assertThat(lastPage.getAnnotations()).isNotEmpty();
    }
  }
}
