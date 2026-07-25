package com.amalakaky.aegiscode.infrastructure.adapter.out.ai;

import java.util.regex.Pattern;

final class PromptInjectionDefense {

  private static final Pattern[] INJECTION_PATTERNS = {
      Pattern.compile("(?i)(?:ignora|ignorar|ignore|omite|olvida|desestima|anula)\\s+(?:las\\s+)?(?:instrucciones\\s+)?(?:anteriores|previas|de\\s+sistema|del\\s+sistema)",
          Pattern.MULTILINE),
      Pattern.compile("(?i)(?:ignore|forget|override|disregard|bypass|skip)\\s+(?:all\\s+)?(?:previous\\s+)?(?:instructions|commands|directives|rules)",
          Pattern.MULTILINE),
      Pattern.compile("(?i)(?:tu\\s+eres|eres|actua\\s+como|ahora\\s+eres|you\\s+are|act\\s+as|from\\s+now\\s+on)",
          Pattern.MULTILINE),
      Pattern.compile("(?i)(?:system\\s+prompt|prompt\\s+injection|jailbreak|dan\\b)",
          Pattern.MULTILINE),
  };

  private PromptInjectionDefense() {}

  static String sanitize(String input) {
    if (input == null || input.isBlank()) {
      return input;
    }
    String result = input;
    for (Pattern p : INJECTION_PATTERNS) {
      result = p.matcher(result).replaceAll("[ POSIBLE INTENTO DE PROMPT INJECTION DETECTADO ]");
    }
    return result;
  }

  static String wrapInDelimiters(String input) {
    String sanitized = sanitize(input);
    return "[INICIO_CODIGO_FUENTE]\n" + sanitized + "\n[FIN_CODIGO_FUENTE]";
  }
}
