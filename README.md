# 🛡️ AegisCode AI - Shift-Left DevSecOps Platform

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2B%20DDD-blue.svg)
![Status](https://img.shields.io/badge/Status-Active%20Development-success.svg)

Plataforma DevSecOps basada en una arquitectura multi-agente de Inteligencia Artificial. Diseñada para integrarse en etapas tempranas del ciclo de desarrollo (*Shift-Left*), interceptando, auditando y refactorizando código vulnerable mediante técnicas de *Clean Code* de forma totalmente automatizada.

Este proyecto constituye el Trabajo de Fin de Máster (TFM).

---

## 🏗️ Arquitectura y Diseño

El núcleo de AegisCode AI se sostiene sobre una **Arquitectura Hexagonal estricta** combinada con **Domain-Driven Design (DDD)**. La capa de dominio es absolutamente agnóstica de frameworks, persistencia o infraestructura externa.

Para asegurar esta integridad, el proyecto integra validaciones de arquitectura en tiempo de compilación mediante `ArchUnit`.

### Topología Multi-Agente (IA)
La orquestación del LLM se divide aplicando el Principio de Responsabilidad Única (SRP) en un ecosistema de agentes deterministas:

* **Scanner Agent:** Extrae el AST del código, descarta ruido y detecta secretos expuestos (Red Team Inicial).
* **Auditor Agent:** Analiza el flujo de datos para confirmar vulnerabilidades lógicas complejas como SQL Injections o Race Conditions (Red Team Avanzado).
* **Remediation Agent:** Genera el parche mitigador aplicando patrones de diseño, respetando métricas de complejidad ciclomática y buenas prácticas de Clean Code (Blue Team).

---

## 🚀 Stack Tecnológico (Enterprise Zero-Cost)

* **Core:** Java 21 + Spring Boot 3.3.0.
* **Orquestación IA:** Spring AI + Google Gemini / Groq API.
* **Persistencia:** Oracle Cloud Autonomous Database + Flyway (Migraciones).
* **Seguridad API:** Protección contra abuso de cuotas LLM mediante Rate Limiting (Bucket4j).
* **Calidad y Testing:** JUnit 5, ArchUnit, Maven Checkstyle.

---

## 🔒 Gobernanza y Calidad de Código (Innegociable)

El código fuente de este proyecto se rige por un conjunto de reglas estáticas estrictas para asegurar un bajo acoplamiento, alta cohesión y una complejidad ciclomática mínima:

1. **Retorno Único:** Máximo un (1) `return` por método para asegurar flujos predecibles.
2. **Ciclomática Controlada:** Máximo tres (3) `if` por método, forzando el uso de polimorfismo o constructos funcionales (`Optional`, Patrón Strategy).
3. **Cero Números Mágicos:** Uso exclusivo de constantes (`static final`) o enums.

### Configuración del Entorno Local (Pre-commit Hook)

Para garantizar la aplicación de estas métricas, el proyecto utiliza un *Git Hook* que ejecuta un análisis *fail-fast* antes de permitir cualquier commit local.

**Instrucción obligatoria tras clonar el repositorio:**
Ejecuta el siguiente comando en la raíz del proyecto para enlazar los hooks versionados con tu entorno local:

```bash
git config core.hooksPath .githooks