package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa {@link GitProviderPort} usando
 * Eclipse JGit para clonar repositorios GitHub.
 *
 * Realiza un clon superficial (shallow clone, depth=1) para minimizar el tiempo
 * de descarga y el espacio en disco. La autenticacion se realiza mediante
 * GitHub Personal Access Token configurado en {@code app.vcs.github.token}.
 */
@Slf4j
@Component
public class JGitAdapter implements GitProviderPort {

  private static final String TEMP_DIR_PREFIX = "aegiscode-repo-";
  private static final String JAVA_EXTENSION = ".java";

  private final String githubToken;

  public JGitAdapter(@Value("${app.vcs.github.token}") String githubToken) {
    this.githubToken = githubToken;
  }

  @Override
  public String fetchSourceFiles(String repositoryUrl, String branch) {
    File tempDir = null;
    String result = "";
    try {
      tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
      log.info("Directorio temporal creado en: {}", tempDir.getAbsolutePath());

      UsernamePasswordCredentialsProvider credentials =
          new UsernamePasswordCredentialsProvider(githubToken, "");

      log.info("Iniciando clonado seguro de {} (Rama: {})", repositoryUrl, branch);
      try (Git git = Git.cloneRepository()
          .setURI(repositoryUrl)
          .setBranch(branch)
          .setDirectory(tempDir)
          .setCredentialsProvider(credentials)
          .setDepth(1)
          .call()) {
        log.info("Clonado exitoso. Extrayendo ficheros Java...");
        result = readJavaFiles(tempDir);
      }
    } catch (Exception e) {
      log.error("Fallo crítico en infraestructura JGit: {}", e.getMessage(), e);
      throw new IllegalStateException(
          "Error al clonar el repositorio: " + repositoryUrl, e);
    } finally {
      deleteDirectory(tempDir);
    }
    return result;
  }

  private String readJavaFiles(File directory) {
    StringBuilder content = new StringBuilder();
    try (Stream<Path> paths = Files.walk(directory.toPath())) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(JAVA_EXTENSION))
          .forEach(path -> {
            try {
              content.append("--- Archivo: ")
                  .append(path.getFileName().toString())
                  .append(" ---\n");
              content.append(Files.readString(path, StandardCharsets.UTF_8))
                  .append("\n\n");
            } catch (IOException e) {
              log.warn("No se pudo leer el archivo {}", path.getFileName());
            }
          });
    } catch (IOException e) {
      log.error("Fallo de I/O al recorrer el repositorio: {}", e.getMessage());
      throw new IllegalStateException("Error al extraer archivos Java", e);
    }
    return content.toString();
  }

  private static void deleteDirectory(File directory) {
    if (directory != null) {
      try (Stream<Path> paths = Files.walk(directory.toPath())) {
        paths.sorted(java.util.Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        log.info("Directorio temporal eliminado: {}", directory.getAbsolutePath());
      } catch (IOException e) {
        log.warn("No se pudo eliminar el directorio temporal: {}",
            directory.getAbsolutePath(), e);
      }
    }
  }
}




