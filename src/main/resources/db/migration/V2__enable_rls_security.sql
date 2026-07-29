-- ============================================
-- Row Level Security (RLS) - Solo para PostgreSQL
-- ============================================
-- NOTA: H2 no soporta RLS. Esta migración se ejecuta solo en producción (PostgreSQL/Supabase).
-- En desarrollo local (H2), esta migración es un NO-OP.
--
-- En producción (PostgreSQL), ejecutar manualmente o via Flyway callbacks:
-- 1. ALTER TABLE audit_reports ENABLE ROW LEVEL SECURITY;
-- 2. DROP POLICY IF EXISTS deny_anon_audit ON audit_reports;
-- 3. CREATE POLICY deny_anon_audit ON audit_reports FOR ALL TO anon, authenticated USING (false);
--
-- Para desarrollo local, no se requiere RLS ya que H2 está en memoria y no es accesible externamente.

-- NO-OP statement compatible con H2 y PostgreSQL
SELECT 1 WHERE 1=0;
