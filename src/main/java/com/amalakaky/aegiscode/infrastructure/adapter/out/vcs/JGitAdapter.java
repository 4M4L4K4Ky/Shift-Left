package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import com.amalakaky.aegiscode.application.port.out.vcs.GitProviderPort;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class JGitAdapter implements GitProviderPort {

    private static final String TEMP_DIR_PREFIX = "aegis-repo-";
    private static final String JAVA_EXTENSION = ".java";
    private static final String GIT_FOLDER = ".git";

    @Override
    public List<File> fetchSourceFiles(String repositoryUrl, String branch) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);

            Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(tempDir.toFile())
                    .setBranch(branch)
                    .setDepth(1)
                    .call();

            return scanJavaFiles(tempDir);

        } catch (IOException | GitAPIException e) {
            // Manejo estricto de excepciones de infraestructura
            throw new IllegalStateException("Failed to fetch and clone repository from: " + repositoryUrl, e);
        }
    }

    private List<File> scanJavaFiles(Path rootPath) throws IOException {
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains(File.separator + GIT_FOLDER + File.separator))
                    .filter(path -> path.toString().endsWith(JAVA_EXTENSION))
                    .map(Path::toFile)
                    .toList();
        }
    }
}