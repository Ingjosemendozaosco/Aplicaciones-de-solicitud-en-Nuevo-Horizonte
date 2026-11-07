-- ============================================================
-- ACTUALIZACIÓN DE BD - ADJUNTOS Y COMENTARIO DE DECISIÓN
-- Base de datos: nuevo-horizonte-solicitudes
-- Ejecutar UNA SOLA VEZ
-- ============================================================

-- 1. Columnas para archivos adjuntos en solicitud
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS nombre_archivo     VARCHAR(255);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS tipo_archivo      VARCHAR(100);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS contenido_archivo BYTEA;

-- 2. Comentario del administrador en la decisión
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS comentario_admin TEXT;

-- 3. Secuencia para numeración de solicitudes (si no existe)
CREATE SEQUENCE IF NOT EXISTS seq_numero_solicitud START WITH 1 INCREMENT BY 1;

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
