-- Vista analítica para el cálculo agregado de severidades
CREATE OR REPLACE VIEW view_vulnerability_severity_stats AS
SELECT
    severity,
    COUNT(*) AS total_count
FROM vulnerabilities
GROUP BY severity;