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
 * 
 * Realiza un clon superficial (shallow clone, depth=1) para minimizar el tiempo
 * de descarga y el espacio en disco. La autenticacion se realiza mediante
 * GitHub Personal Access Token configurado en {@code app.vcs.github.token}.
 */
@Slf4j
@Component
public class JGitAdapter implements GitProviderPort {

  private final String githubToken;

  public JGitAdapter(@Value("${app.vcs.github.token}") String githubToken) {
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
      if (tempDir != null) {
        try (Stream<Path> paths = Files.walk(tempDir.toPath())) {
          paths.sorted(java.util.Comparator.reverseOrder())
              .map(Path::toFile)
              .forEach(File::delete);
          log.info("Directorio temporal eliminado: {}", tempDir.getAbsolutePath());
        } catch (IOException e) {
          log.warn("No se pudo eliminar el directorio temporal: {}", tempDir.getAbsolutePath(), e);
        }
      }
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




