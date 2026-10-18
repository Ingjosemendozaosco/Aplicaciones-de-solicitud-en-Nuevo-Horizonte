-- ============================================================
-- SCRIPT COMPLETO - ACTUALIZACIÓN DE BD PARA ADJUNTOS Y REQUISITOS
-- Base de datos: nuevo-horizonte
-- Ejecutar UNA SOLA VEZ
-- ============================================================

-- 1. AGREGAR COLUMNAS PARA ARCHIVOS ADJUNTOS EN TABLA SOLICITUD
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS nombre_archivo     VARCHAR(255);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS tipo_archivo      VARCHAR(100);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS contenido_archivo BYTEA;

-- 2. VERIFICAR EXISTENCIA DE SECUENCIA PARA NÚMEROS DE SOLICITUD
CREATE SEQUENCE IF NOT EXISTS seq_numero_solicitud START WITH 1 INCREMENT BY 1;

-- 3. COMENTARIOS (OPCIONAL, PARA DOCUMENTACIÓN)
COMMENT ON COLUMN solicitud.nombre_archivo IS 'Nombre original del archivo adjunto';
COMMENT ON COLUMN solicitud.tipo_archivo IS 'Tipo MIME del archivo adjunto (image/jpeg, application/pdf, etc.)';
COMMENT ON COLUMN solicitud.contenido_archivo IS 'Contenido binario del archivo adjunto (BYTEA en PostgreSQL)';

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================