-- Migración: vigencia de roles/permisos + detalle de accesos (PostgreSQL)
-- Ejecutar UNA sola vez en la base "nuevo-horizonte-solicitudes".
-- Hibernate (ddl-auto=update) crea las columnas/tablas nuevas automáticamente;
-- este script es un respaldo/documentación y migra los datos existentes.

-- 1. Nueva tabla de asignación usuario-rol con vigencia
CREATE TABLE IF NOT EXISTS usuario_rol_asignacion (
    id_usuario_rol BIGSERIAL PRIMARY KEY,
    id_usuario     BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_rol         BIGINT NOT NULL REFERENCES rol(id_rol),
    tipo_vigencia  VARCHAR(20) NOT NULL DEFAULT 'DEFINITIVO',
    fecha_inicio   DATE,
    fecha_fin      DATE,
    activo         BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2. Copiar roles existentes (join table antigua usuario_rol) como DEFINITIVO
INSERT INTO usuario_rol_asignacion (id_usuario, id_rol, tipo_vigencia, fecha_inicio, activo)
SELECT ur.id_usuario, ur.id_rol, 'DEFINITIVO', CURRENT_DATE, TRUE
FROM usuario_rol ur
WHERE NOT EXISTS (
    SELECT 1 FROM usuario_rol_asignacion a
    WHERE a.id_usuario = ur.id_usuario AND a.id_rol = ur.id_rol
);

-- 3. Vigencia en permisos de rol
ALTER TABLE rol_permiso ADD COLUMN IF NOT EXISTS tipo_vigencia    VARCHAR(20) DEFAULT 'DEFINITIVO';
ALTER TABLE rol_permiso ADD COLUMN IF NOT EXISTS fecha_inicio     DATE;
ALTER TABLE rol_permiso ADD COLUMN IF NOT EXISTS fecha_fin        DATE;
ALTER TABLE rol_permiso ADD COLUMN IF NOT EXISTS activo           BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE rol_permiso ADD COLUMN IF NOT EXISTS origen_solicitud VARCHAR(20);

-- 4. Vigencia de accesos en solicitudes + recordatorio SLA
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS fecha_inicio_acceso    DATE;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS fecha_fin_acceso       DATE;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS fecha_recordatorio_sla TIMESTAMP;

-- 5. Detalle módulo+nivel de cada solicitud de acceso
CREATE TABLE IF NOT EXISTS solicitud_acceso_detalle (
    id_detalle   BIGSERIAL PRIMARY KEY,
    id_solicitud BIGINT NOT NULL REFERENCES solicitud(id_solicitud),
    modulo       VARCHAR(60) NOT NULL,
    nivel        VARCHAR(20) NOT NULL
);

-- 6. Informe entregado por el administrador (solicitudes de información)
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS nombre_informe      VARCHAR(255);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS tipo_informe        VARCHAR(100);
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS contenido_informe   BYTEA;

-- 7. Acceso permanente y contrapropuesta del administrador
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS permanente            BOOLEAN DEFAULT FALSE;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS propuesta_permanente  BOOLEAN;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS propuesta_fecha_inicio DATE;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS propuesta_fecha_fin    DATE;
ALTER TABLE solicitud ADD COLUMN IF NOT EXISTS nota_propuesta         TEXT;
UPDATE solicitud SET permanente = FALSE WHERE permanente IS NULL;

CREATE TABLE IF NOT EXISTS solicitud_propuesta_detalle (
    id_detalle   BIGSERIAL PRIMARY KEY,
    id_solicitud BIGINT NOT NULL REFERENCES solicitud(id_solicitud),
    modulo       VARCHAR(60) NOT NULL,
    nivel        VARCHAR(20) NOT NULL
);
