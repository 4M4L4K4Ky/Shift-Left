# 🛡️ AegisCode AI - Shift-Left DevSecOps Platform

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2B%20DDD-blue.svg)
![Database](https://img.shields.io/badge/Database-Supabase%20%7C%20PostgreSQL-3ECF8E.svg)
![AI](https://img.shields.io/badge/AI-Spring%20AI%20%2B%20Groq-4285F4.svg)
![Status](https://img.shields.io/badge/Status-Active%20Development-success.svg)

Plataforma DevSecOps basada en una arquitectura multi-agente de Inteligencia Artificial. Diseñada para integrarse en etapas tempranas del ciclo de desarrollo (*Shift-Left*), interceptando, auditando y refactorizando código vulnerable mediante técnicas de *Clean Code* de forma totalmente automatizada.

Este proyecto constituye el Trabajo de Fin de Máster (TFM).

---

## 🏗️ Arquitectura y Diseño

El núcleo de AegisCode AI se sostiene sobre una **Arquitectura Hexagonal (Ports and Adapters)** estricta, combinada con **Domain-Driven Design (DDD)**. La capa de dominio es absolutamente agnóstica de frameworks, infraestructuras externas o anotaciones de persistencia.

*   **Aislamiento del Dominio:** Las entidades core (como `AuditReport` o el Value Object `SeverityScore`) están protegidas de la infraestructura. El mapeo a DTOs (capa REST) y a Entidades JPA (capa de BD) se realiza en los límites de los adaptadores, garantizando una inmutabilidad total.
*   **Gestión de Perfiles Estratégica:** Patrón de repositorio unificado que permite ejecución local ultrarrápida (H2 en memoria modo PostgreSQL) y despliegue en la nube transparente (Supabase).
*   **Validación Estática:** Integración de validaciones de arquitectura en tiempo de compilación mediante `ArchUnit`.

### Topología Multi-Agente (IA)
La orquestación del LLM se divide aplicando el Principio de Responsabilidad Única (SRP) en un ecosistema de agentes deterministas:

*   **Scanner Agent:** Extrae el AST del código, descarta ruido y detecta secretos expuestos (Red Team Inicial).
*   **Auditor Agent:** Analiza el flujo de datos para confirmar vulnerabilidades lógicas complejas como SQL Injections o Race Conditions (Red Team Avanzado).
*   **Remediation Agent:** Genera el parche mitigador aplicando patrones de diseño, respetando métricas de complejidad ciclomática y buenas prácticas de Clean Code (Blue Team).

---

## 🔒 Seguridad por Diseño (DevSecOps)

Al tratarse de una herramienta de ciberseguridad, la propia plataforma aplica estrategias de **Defensa en Profundidad (Defense in Depth)** en todas sus capas:

1.  **Protección LLM (Prompt Injection):** El adaptador de salida de IA sanitiza el código fuente recibido neutralizando delimitadores maliciosos e impone un contrato de salida estricto en JSON, evitando que alucinaciones del modelo rompan la ejecución del backend.
2.  **Seguridad API:** Protección contra el abuso de cuotas del LLM y ataques de denegación de servicio (DoS) mediante políticas de Rate Limiting.

### 💾 Estrategia de Persistencia y Zero Trust (Supabase & Flyway)

La capa de infraestructura de base de datos está gestionada íntegramente por **Flyway** para garantizar la idempotencia de los despliegues.

Al operar en producción sobre un BaaS como Supabase, el diseño asume un modelo de amenaza donde los endpoints de PostgREST podrían quedar expuestos. Para mitigarlo, Flyway inyecta automáticamente una migración de hardening (`V2__enable_rls_security.sql`) que fuerza **Row Level Security (RLS)** a nivel de motor SQL:

```sql
-- 1. Habilitar RLS en la tabla de reportes de auditoría
ALTER TABLE audit_reports ENABLE ROW LEVEL SECURITY;

-- 2. Habilitar RLS en la tabla de control interno de Flyway
ALTER TABLE flyway_schema_history ENABLE ROW LEVEL SECURITY;

-- 3. Denegar explícitamente el acceso a los roles expuestos a internet por Supabase
-- Esto garantiza que solo tu backend de Spring Boot (conectado con rol de postgres/servicio) pueda operar.
DROP POLICY IF EXISTS deny_anon_audit ON audit_reports;
CREATE POLICY deny_anon_audit ON audit_reports FOR ALL TO anon, authenticated USING (false);

DROP POLICY IF EXISTS deny_anon_flyway ON flyway_schema_history;
CREATE POLICY deny_anon_flyway ON flyway_schema_history FOR ALL TO anon, authenticated USING (false);
```

Esto asegura que la base de datos sea inexpugnable desde el exterior; **solo el backend de Spring Boot** está autorizado para realizar operaciones I/O.

---

## ⚙️ Integración Continua (CI/CD) y Pipeline Automatizado

Para garantizar la inmutabilidad y la calidad del código en cada iteración, el proyecto implementa un pipeline automatizado mediante **GitHub Actions**. Este flujo actúa como una barrera *fail-fast* para las ramas críticas (`main` y `develop`).

El pipeline provisiona JDK 21 y ejecuta la fase `verify` de Maven, asegurando:
*   Pase completo de la batería de pruebas unitarias y de integración.
*   Disparo automático de las reglas de **ArchUnit** para proteger los límites de la arquitectura hexagonal.
*   Inyección dinámica de credenciales vía GitHub Secrets para los conectores de base de datos.

---

## ⚖️ Gobernanza y Calidad de Código (Innegociable)

El código fuente de este proyecto se rige por un conjunto de reglas estáticas estrictas:

1.  **Retorno Único:** Máximo un (1) `return` por método para asegurar flujos predecibles.
2.  **Ciclomática Controlada:** Máximo tres (3) `if` por método, forzando el uso de polimorfismo o constructos funcionales (`Optional`, Patrón Strategy).
3.  **Cero Números Mágicos:** Uso exclusivo de constantes (`static final`) o enums.
4.  **Inmutabilidad por Defecto:** Uso extensivo de `records` de Java y clases `final` inmutables (Lombok `@Builder`).

**Instrucción obligatoria tras clonar el repositorio para habilitar el Pre-commit Hook:**
```bash
git config core.hooksPath .githooks
```

---

## 🚀 Ejecución y Despliegue (Perfiles)

La aplicación soporta despliegue multi-entorno gracias al sistema de perfiles de Spring Boot.

### Configuración de Arranque (Run/Debug Configurations en IntelliJ)
Recuerda aplicar obligatoriamente el parámetro de red IPv4 en las VM options para evitar fallos de conectividad con los servicios externos:
* **VM options:** `-Djava.net.preferIPv4Stack=true`

### Entorno Local (TDD y Pruebas Rápidas)
Levanta la aplicación utilizando H2 In-Memory configurado en modo compatibilidad PostgreSQL. No requiere contenedores adicionales.

**Vía Maven:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
**Vía JAR compilado:**
```bash
java -Djava.net.preferIPv4Stack=true -jar target/aegiscode-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Entorno Producción (Integración Supabase + Groq)
Ataca directamente a los recursos en la nube. Requiere inyectar las variables de entorno de seguridad (`SUPABASE_PASSWORD`, `GROQ_API_KEY`).

**Vía Maven:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
**Vía JAR compilado:**
```bash
java -Djava.net.preferIPv4Stack=true -jar target/aegiscode-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```