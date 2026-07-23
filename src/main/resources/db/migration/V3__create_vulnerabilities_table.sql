-- 1. Crear la tabla relacional de vulnerabilidades vinculada a audit_reports
CREATE TABLE vulnerabilities (
                                 id BIGSERIAL PRIMARY KEY,
                                 scan_id VARCHAR(36) NOT NULL,
                                 cwe_id VARCHAR(50) NOT NULL,
                                 severity INT NOT NULL,
                                 description TEXT,
                                 remediation_patch TEXT,
                                 CONSTRAINT fk_audit_report
                                     FOREIGN KEY (scan_id)
                                         REFERENCES audit_reports(scan_id)
                                         ON DELETE CASCADE
);

-- 2. Habilitar RLS en la nueva tabla (Seguridad por Diseño)
ALTER TABLE vulnerabilities ENABLE ROW LEVEL SECURITY;

-- 3. Denegar el acceso directo desde la API de Supabase (PostgREST) a roles públicos/autenticados
DROP POLICY IF EXISTS deny_anon_vulnerabilities ON vulnerabilities;
CREATE POLICY deny_anon_vulnerabilities
    ON vulnerabilities
    FOR ALL
    TO anon, authenticated
    USING (false);

-- 4. Índice para optimizar las búsquedas por scan_id en los joins de Hibernate
CREATE INDEX idx_vulnerabilities_scan_id ON vulnerabilities(scan_id);