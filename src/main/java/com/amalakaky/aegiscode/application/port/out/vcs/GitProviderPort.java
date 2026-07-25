package com.amalakaky.aegiscode.application.port.out.vcs;

import java.io.File;
import java.util.List;

public interface GitProviderPort {

  /**
   * Clona o descarga de forma temporal un repositorio remoto y extrae
   * los ficheros de código fuente relevantes para la auditoría.
   *
     * @param repositoryUrl URL HTTPS o SSH del repositorio Git.
     * @param branch Rama específica a auditar (ej. main, develop).
   * @return Lista de archivos fuente filtrados listos para el análisis AST.
   */
  List<File> fetchSourceFiles(String repositoryUrl, String branch);
}



