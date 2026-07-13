-- Datos semilla (catálogos) para el Sistema de Gestión de Visitas Académicas.
-- Ejecutar UNA VEZ después de crear las tablas; sin estos registros fallan los
-- INSERT de usuarios (FK a ROL) y de solicitudes (FK a ESTADO_SOLICITUD).

-- Roles
INSERT INTO rol (nombre_rol) VALUES ('Docente');
INSERT INTO rol (nombre_rol) VALUES ('Estadias');
INSERT INTO rol (nombre_rol) VALUES ('Administrador');

-- Estados de la solicitud
INSERT INTO estado_solicitud (nombre_estado) VALUES ('Pendiente');
INSERT INTO estado_solicitud (nombre_estado) VALUES ('En revisión');
INSERT INTO estado_solicitud (nombre_estado) VALUES ('Aprobada');
INSERT INTO estado_solicitud (nombre_estado) VALUES ('Rechazada');
INSERT INTO estado_solicitud (nombre_estado) VALUES ('Completada');

-- Estados del reporte
INSERT INTO estado_reporte (nombre_estado) VALUES ('Pendiente');
INSERT INTO estado_reporte (nombre_estado) VALUES ('Completado');
INSERT INTO estado_reporte (nombre_estado) VALUES ('Aprobado');
INSERT INTO estado_reporte (nombre_estado) VALUES ('Rechazado');

-- Tipos de documento
INSERT INTO tipo_documento (nombre_tipo) VALUES ('FO-UTEZ-EST-08 firmado');
INSERT INTO tipo_documento (nombre_tipo) VALUES ('Carta responsiva');
INSERT INTO tipo_documento (nombre_tipo) VALUES ('Oficio de aceptación');

COMMIT;
