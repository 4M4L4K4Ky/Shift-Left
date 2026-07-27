package com.amalakaky.aegiscode.infrastructure.adapter.out.vcs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
  void fetchSourceFiles_shouldReturnJavaFilesWhenCloneSucceeds() {
    Path tempDirPath = Path.of("/tmp/test-repo");
    File javaFile1 = new File("Test.java");
    File javaFile2 = new File("Main.java");
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
      when(walkStream.map(any())).thenReturn(walkStream);
      when(walkStream.toList()).thenReturn(List.of(javaFile1, javaFile2));

      Stream cleanupStream = mock(Stream.class);
      when(cleanupStream.sorted(any())).thenReturn(cleanupStream);
      when(cleanupStream.map(any())).thenReturn(cleanupStream);

      filesMock.when(() -> Files.walk(tempDirPath)).thenReturn(walkStream, cleanupStream);

      var adapter = new JGitAdapter("dummy-token");
      var result = adapter.fetchSourceFiles("https://github.com/test/repo.git", "main");

      assertThat(result).hasSize(2);
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
  void extractJavaFiles_shouldReturnJavaFiles() {
    var tempDir = java.nio.file.Files.createTempDirectory("jgit-test").toFile();
    try {
      new File(tempDir, "Test.java").createNewFile();
      new File(tempDir, "Main.java").createNewFile();
      new File(tempDir, "notes.txt").createNewFile();

      var adapter = new JGitAdapter("dummy-token");
      Method method = JGitAdapter.class.getDeclaredMethod("extractJavaFiles", File.class);
      method.setAccessible(true);
      var result = (List<File>) method.invoke(adapter, tempDir);

      assertThat(result).hasSize(2);
      assertThat(result).allMatch(f -> f.getName().endsWith(".java"));
    } finally {
      for (var f : tempDir.listFiles()) f.delete();
      tempDir.delete();
    }
  }

  @SneakyThrows
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void extractJavaFiles_shouldThrowIllegalStateWhenIoException() {
    var tempDir = new File("/tmp/repo");

    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.walk(tempDir.toPath())).thenThrow(new IOException("disk full"));

      var adapter = new JGitAdapter("dummy-token");
      Method method = JGitAdapter.class.getDeclaredMethod("extractJavaFiles", File.class);
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
}
