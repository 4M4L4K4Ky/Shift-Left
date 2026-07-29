package com.amalakaky.aegiscode.application.port.out.vcs;

public interface GitProviderPort {

  /**
   * Clona un repositorio remoto y retorna el contenido concatenado de los
   * archivos fuente relevantes para la auditoria.
   *
   * @param repositoryUrl URL HTTPS del repositorio Git.
   * @param branch        Rama a auditar (ej. main, develop).
   * @return Contenido de todos los archivos fuente concatenados.
   */
  String fetchSourceFiles(String repositoryUrl, String branch);
}



