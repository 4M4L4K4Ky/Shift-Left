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

@Slf4j
@Component
public class JgitAdapter implements GitProviderPort {

  private final String githubToken;

  // Inyectamos el token de forma segura
  public JgitAdapter(@Value("${app.vcs.github.token}") String githubToken) {
    this.githubToken = githubToken;
  }

  @Override
  public List<File> fetchSourceFiles(String repositoryUrl, String branch) {
    File tempDir = null;
    try {
      // 1. Crear directorio temporal seguro (Mitigación DoS)
      tempDir = Files.createTempDirectory("aegiscode-repo-").toFile();
      log.info("Directorio temporal creado en: {}", tempDir.getAbsolutePath());

      // 2. Configurar credenciales DevSecOps (PAT de GitHub)
      UsernamePasswordCredentialsProvider credentials =
          new UsernamePasswordCredentialsProvider(githubToken, "");

      // 3. Clonado superficial (Shallow Clone) para máximo rendimiento
      log.info("Iniciando clonado seguro de {} (Rama: {})", repositoryUrl, branch);
      try (Git git = Git.cloneRepository()
          .setURI(repositoryUrl)
          .setBranch(branch)
          .setDirectory(tempDir)
          .setCredentialsProvider(credentials)
          .setDepth(1) // VITAL: Ahorra red y almacenamiento
          .call()) {

        log.info("Clonado exitoso. Extrayendo ficheros Java...");
        // Aquí llamas a tu lógica para recorrer 'tempDir' y sacar los .java
        return extractJavaFiles(tempDir);
      }

    } catch (Exception e) {
      log.error("Fallo crítico en infraestructura JGit: {}", e.getMessage(), e);
      throw new IllegalStateException("Error al clonar el repositorio: " + repositoryUrl, e);
    } finally {
      // IMPORTANTE: Un buen arquitecto siempre limpia la basura.
      // Asegúrate de que el caso de uso o este adapter borre el tempDir tras pasárselo al LLM.
    }
  }

  // Método dummy, asumo que ya tienes implementada la búsqueda recursiva de archivos .java
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




