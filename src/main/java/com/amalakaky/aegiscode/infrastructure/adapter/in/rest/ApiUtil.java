package com.amalakaky.aegiscode.infrastructure.adapter.in.rest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Utilidad para escribir respuestas de ejemplo en la documentación OpenAPI.
 * 
 * Usada internamente por el código generado para poblar la consola de Swagger
 * con ejemplos de respuesta.
 */
public class ApiUtil {
  public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
    try {
      HttpServletResponse res = req.getNativeResponse(HttpServletResponse.class);
      res.setCharacterEncoding("UTF-8");
      res.addHeader("Content-Type", contentType);
      res.getWriter().print(example);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}




