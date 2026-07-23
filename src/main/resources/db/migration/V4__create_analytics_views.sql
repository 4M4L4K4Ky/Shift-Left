-- Creamos una vista optimizada en PostgreSQL que encapsula el GROUP BY
CREATE OR REPLACE VIEW view_vulnerability_severity_stats AS
SELECT
    severity,
    COUNT(*) AS total_count
FROM vulnerabilities
GROUP BY severity;