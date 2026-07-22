-- 1. Habilitar RLS en la tabla de reportes de auditoria
ALTER TABLE audit_reports ENABLE ROW LEVEL SECURITY;

-- 2. Denegar explicitamente el acceso a los roles expuestos a internet por Supabase
DROP POLICY IF EXISTS deny_anon_audit ON audit_reports;
CREATE POLICY deny_anon_audit ON audit_reports FOR ALL TO anon, authenticated USING (false);