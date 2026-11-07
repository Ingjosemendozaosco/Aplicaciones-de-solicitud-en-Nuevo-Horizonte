-- Migración: adjuntos en solicitud (ejecutar una sola vez en la BD "nuevo-horizonte")
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS nombre_archivo     VARCHAR(255);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS tipo_archivo      VARCHAR(100);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS contenido_archivo BYTEA;
