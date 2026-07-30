package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JGitAdapterTest {

  @SneakyThrows
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void fetchSourceFiles_shouldReturnConcatenatedContentWhenCloneSucceeds() {
    Path tempDirPath = Path.of("/tmp/test-repo");
    Path javaPath1 = tempDirPath.resolve("Test.java");
    Path javaPath2 = tempDirPath.resolve("Main.java");
    CloneCommand cloneCmd = mock(CloneCommand.class);
    Git git = mock(Git.class);

    when(cloneCmd.setURI(any())).thenReturn(cloneCmd);
    when(cloneCmd.setBranch(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDirectory(any())).thenReturn(cloneCmd);
    when(cloneCmd.setCredentialsProvider(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDepth(anyInt())).thenReturn(cloneCmd);
    when(cloneCmd.call()).thenReturn(git);

    try (MockedStatic<Files> filesMock = mockStatic(Files.class);
         MockedStatic<Git> gitMock = mockStatic(Git.class)) {

      gitMock.when(Git::cloneRepository).thenReturn(cloneCmd);
      filesMock.when(() -> Files.createTempDirectory("aegiscode-repo-")).thenReturn(tempDirPath);

      Stream walkStream = mock(Stream.class);
      when(walkStream.filter(any())).thenReturn(walkStream);
      doAnswer(invocation -> {
        java.util.function.Consumer consumer = invocation.getArgument(0);
        consumer.accept(javaPath1);
        consumer.accept(javaPath2);
        return null;
      }).when(walkStream).forEach(any());

      filesMock.when(() -> Files.readString(javaPath1, java.nio.charset.StandardCharsets.UTF_8))
          .thenReturn("class Test {}");
      filesMock.when(() -> Files.readString(javaPath2, java.nio.charset.StandardCharsets.UTF_8))
          .thenReturn("class Main {}");

      Stream cleanupStream = mock(Stream.class);
      when(cleanupStream.sorted(any())).thenReturn(cleanupStream);
      when(cleanupStream.map(any())).thenReturn(cleanupStream);

      filesMock.when(() -> Files.walk(tempDirPath))
          .thenReturn(walkStream, cleanupStream);

      var adapter = new JGitAdapter("dummy-token");
      var result = adapter.fetchSourceFiles("https://github.com/test/repo.git", "main");

      assertThat(result)
          .contains("--- Archivo: Test.java ---")
          .contains("class Test {}")
          .contains("--- Archivo: Main.java ---")
          .contains("class Main {}");
    }
  }

  @SneakyThrows
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void fetchSourceFiles_shouldThrowIllegalStateWhenCloneFails() {
    Path tempDirPath = Path.of("/tmp/test-repo");
    CloneCommand cloneCmd = mock(CloneCommand.class);

    when(cloneCmd.setURI(any())).thenReturn(cloneCmd);
    when(cloneCmd.setBranch(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDirectory(any())).thenReturn(cloneCmd);
    when(cloneCmd.setCredentialsProvider(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDepth(anyInt())).thenReturn(cloneCmd);
    when(cloneCmd.call()).thenThrow(new RuntimeException("clone failed"));

    try (MockedStatic<Files> filesMock = mockStatic(Files.class);
         MockedStatic<Git> gitMock = mockStatic(Git.class)) {

      gitMock.when(Git::cloneRepository).thenReturn(cloneCmd);
      filesMock.when(() -> Files.createTempDirectory("aegiscode-repo-")).thenReturn(tempDirPath);

      Stream cleanupStream = mock(Stream.class);
      when(cleanupStream.sorted(any())).thenReturn(cleanupStream);
      when(cleanupStream.map(any())).thenReturn(cleanupStream);
      filesMock.when(() -> Files.walk(tempDirPath)).thenReturn(cleanupStream);

      var adapter = new JGitAdapter("dummy-token");
      assertThatThrownBy(() ->
          adapter.fetchSourceFiles("https://github.com/test/repo.git", "main"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Error al clonar el repositorio");
    }
  }

  @SneakyThrows
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void fetchSourceFiles_shouldSwallowCleanupErrorWhenCloneFails() {
    Path tempDirPath = Path.of("/tmp/test-repo");
    CloneCommand cloneCmd = mock(CloneCommand.class);

    when(cloneCmd.setURI(any())).thenReturn(cloneCmd);
    when(cloneCmd.setBranch(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDirectory(any())).thenReturn(cloneCmd);
    when(cloneCmd.setCredentialsProvider(any())).thenReturn(cloneCmd);
    when(cloneCmd.setDepth(anyInt())).thenReturn(cloneCmd);
    when(cloneCmd.call()).thenThrow(new RuntimeException("clone failed"));

    try (MockedStatic<Files> filesMock = mockStatic(Files.class);
         MockedStatic<Git> gitMock = mockStatic(Git.class)) {

      gitMock.when(Git::cloneRepository).thenReturn(cloneCmd);
      filesMock.when(() -> Files.createTempDirectory("aegiscode-repo-")).thenReturn(tempDirPath);
      filesMock.when(() -> Files.walk(tempDirPath)).thenThrow(new IOException("cleanup failed"));

      var adapter = new JGitAdapter("dummy-token");
      assertThatThrownBy(() ->
          adapter.fetchSourceFiles("https://github.com/test/repo.git", "main"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Error al clonar el repositorio");
    }
  }

  @SneakyThrows
  @Test
  void fetchSourceFiles_shouldThrowIllegalStateWhenTempDirCreationFails() {
    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.createTempDirectory("aegiscode-repo-"))
          .thenThrow(new IOException("disk full"));

      var adapter = new JGitAdapter("dummy-token");
      assertThatThrownBy(() ->
          adapter.fetchSourceFiles("https://github.com/test/repo.git", "main"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Error al clonar el repositorio");
    }
  }

  @SneakyThrows
  @Test
  void readJavaFiles_shouldReturnConcatenatedContent() {
    var tempDir = java.nio.file.Files.createTempDirectory("jgit-test").toFile();
    try {
      File testFile = new File(tempDir, "Test.java");
      File mainFile = new File(tempDir, "Main.java");
      testFile.createNewFile();
      mainFile.createNewFile();
      new File(tempDir, "notes.txt").createNewFile();
      java.nio.file.Files.writeString(testFile.toPath(), "class Test {}");
      java.nio.file.Files.writeString(mainFile.toPath(), "class Main {}");

      var adapter = new JGitAdapter("dummy-token");
      Method method = JGitAdapter.class.getDeclaredMethod("readJavaFiles", File.class);
      method.setAccessible(true);
      var result = (String) method.invoke(adapter, tempDir);

      assertThat(result)
          .contains("--- Archivo: Test.java ---")
          .contains("class Test {}")
          .contains("--- Archivo: Main.java ---")
          .contains("class Main {}");
    } finally {
      for (var f : tempDir.listFiles()) {
        f.delete();
      }
      tempDir.delete();
    }
  }

  @SneakyThrows
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void readJavaFiles_shouldThrowIllegalStateWhenIoException() {
    var tempDir = new File("/tmp/repo");

    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.walk(tempDir.toPath())).thenThrow(new IOException("disk full"));

      var adapter = new JGitAdapter("dummy-token");
      Method method = JGitAdapter.class.getDeclaredMethod("readJavaFiles", File.class);
      method.setAccessible(true);

      assertThatThrownBy(() -> {
        try {
          method.invoke(adapter, tempDir);
        } catch (InvocationTargetException e) {
          throw (IllegalStateException) e.getCause();
        }
      }).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Error al extraer archivos Java");
    }
  }
    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void readJavaFiles_shouldHandleIOExceptionWhenReadingSingleFile() {
        Path tempDirPath = Path.of("/tmp/test-repo");
        Path readablePath = tempDirPath.resolve("Readable.java");
        Path unreadablePath = tempDirPath.resolve("Unreadable.java");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Stream walkStream = mock(Stream.class);
            when(walkStream.filter(any())).thenReturn(walkStream);

            // Simulamos la iteración pasando un archivo legible y uno corrupto/ilegible
            doAnswer(invocation -> {
                java.util.function.Consumer consumer = invocation.getArgument(0);
                consumer.accept(readablePath);
                consumer.accept(unreadablePath);
                return null;
            }).when(walkStream).forEach(any());

            filesMock.when(() -> Files.walk(tempDirPath)).thenReturn(walkStream);

            // El primero se lee bien
            filesMock.when(() -> Files.readString(readablePath, java.nio.charset.StandardCharsets.UTF_8))
                    .thenReturn("class Readable {}");

            // El segundo lanza IOException para forzar el bloque catch interno
            filesMock.when(() -> Files.readString(unreadablePath, java.nio.charset.StandardCharsets.UTF_8))
                    .thenThrow(new IOException("Error de lectura de disco"));

            var adapter = new JGitAdapter("dummy-token");
            Method method = JGitAdapter.class.getDeclaredMethod("readJavaFiles", File.class);
            method.setAccessible(true);
            var result = (String) method.invoke(adapter, tempDirPath.toFile());

            assertThat(result)
                    .contains("--- Archivo: Readable.java ---")
                    .contains("class Readable {}")
                    .doesNotContain("class Unreadable {}");
        }
    }
}
