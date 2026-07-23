package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Component
public class JGitAdapter implements GitProviderPort {

    private final String githubToken;

    // Inyectamos el token de forma segura
    public JGitAdapter(@Value("${app.vcs.github.token}") String githubToken) {
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
        // Tu lógica de filtrado de archivos aquí
        return List.of();
    }
}
