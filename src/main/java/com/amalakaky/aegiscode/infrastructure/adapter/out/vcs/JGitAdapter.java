package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa {@link GitProviderPort} usando
 * Eclipse JGit para clonar repositorios GitHub.
 * <p>
 * Realiza un clon superficial (shallow clone, depth=1) para minimizar el tiempo
 * de descarga y el espacio en disco. La autenticación se realiza mediante
 * GitHub Personal Access Token configurado en {@code app.vcs.github.token}.
 * <p>
 * <b>Pendiente:</b> la limpieza del directorio temporal no está implementada
 * en el bloque {@code finally}.
 */
@Slf4j
@Component
public class JgitAdapter implements GitProviderPort {

  private final String githubToken;

  public JgitAdapter(@Value("${app.vcs.github.token}") String githubToken) {
    this.githubToken = githubToken;
  }

  @Override
  public List<File> fetchSourceFiles(String repositoryUrl, String branch) {
    File tempDir = null;
    try {
      tempDir = Files.createTempDirectory("aegiscode-repo-").toFile();
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
        return extractJavaFiles(tempDir);
      }

    } catch (Exception e) {
      log.error("Fallo crítico en infraestructura JGit: {}", e.getMessage(), e);
      throw new IllegalStateException("Error al clonar el repositorio: " + repositoryUrl, e);
    } finally {
      // TODO: limpiar directorio temporal tempDir después de procesar los archivos
    }
  }

  /** Recorre recursivamente el directorio del repo y retorna todos los archivos .java. */
  private List<File> extractJavaFiles(File directory) {
    try (Stream<Path> paths = Files.walk(directory.toPath())) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .map(Path::toFile)
          .toList();
    } catch (IOException e) {
      log.error("Fallo de I/O al recorrer el repositorio: {}", e.getMessage());
      throw new IllegalStateException("Error al extraer archivos Java", e);
    }
  }
}




