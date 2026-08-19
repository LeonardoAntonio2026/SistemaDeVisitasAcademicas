COMMIT
    INSERT INTO tipo_documento (nombre_tipo)
SELECT 'Reporte de visita firmado' FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM tipo_documento WHERE nombre_tipo = 'Reporte de visita firmado'
)
    INSERT INTO USUARIO (ID_ROL, NOMBRE, CORREO) VALUES(4,'Admin sgva','administrador.prueba@utez.edu.mx')
INSERT INTO ROL (NOMBRE_ROL) VALUES ('ADMINISTSRADOR')
CREATE TABLE DOCUMENTO (
                           ID_DOCUMENTO      NUMBER GENERATED ALWAYS AS IDENTITY,
                           ID_SOLICITUD      NUMBER,
                           ID_REPORTE        NUMBER,
                           ID_TIPO_DOCUMENTO NUMBER NOT NULL,
                           CONTENIDO_BASE64  CLOB NOT NULL,
                           FECHA_CARGA       DATE DEFAULT SYSDATE NOT NULL,

                           CONSTRAINT PK_DOCUMENTO PRIMARY KEY (ID_DOCUMENTO),
                           CONSTRAINT FK_DOCUMENTO_SOLICITUD FOREIGN KEY (ID_SOLICITUD)
                               REFERENCES SOLICITUD (ID_SOLICITUD),
                           CONSTRAINT FK_DOCUMENTO_REPORTE FOREIGN KEY (ID_REPORTE)
                               REFERENCES REPORTE (ID_REPORTE),
                           CONSTRAINT FK_DOCUMENTO_TIPO FOREIGN KEY (ID_TIPO_DOCUMENTO)
                               REFERENCES TIPO_DOCUMENTO (ID_TIPO_DOCUMENTO),
                           CONSTRAINT CHK_DOCUMENTO_ORIGEN CHECK (
                               (ID_SOLICITUD IS NOT NULL AND ID_REPORTE IS NULL) OR
                               (ID_SOLICITUD IS NULL AND ID_REPORTE IS NOT NULL)
                               )
)
CREATE TABLE TIPO_DOCUMENTO (
                                ID_TIPO_DOCUMENTO  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                NOMBRE_TIPO        VARCHAR2(50) NOT NULL UNIQUE
)
CREATE TABLE IMAGEN (
                        ID_IMAGEN        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        ID_REPORTE       NUMBER NOT NULL,
                        CONTENIDO_BASE64 CLOB NOT NULL,
                        FECHA_CARGA      DATE DEFAULT SYSDATE,
                        CONSTRAINT FK_IMAGEN_REPORTE FOREIGN KEY (ID_REPORTE)
                            REFERENCES REPORTE (ID_REPORTE)
)
CREATE TABLE REPORTE (
                         ID_REPORTE       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         ID_SOLICITUD     NUMBER NOT NULL,
                         FECHA            DATE NOT NULL,
                         RESULTADOS       VARCHAR2(4000),
                         OBSERVACIONES    VARCHAR2(1000),
                         FECHA_CREACION   DATE DEFAULT SYSDATE,
                         ID_ESTADO        NUMBER NOT NULL,
                         MOTIVO           VARCHAR2(500),
                         CONSTRAINT FK_REPORTE_SOLICITUD FOREIGN KEY (ID_SOLICITUD)
                             REFERENCES SOLICITUD (ID_SOLICITUD),
                         CONSTRAINT FK_REPORTE_ESTADO FOREIGN KEY (ID_ESTADO)
                             REFERENCES ESTADO_REPORTE (ID_ESTADO)
)
CREATE TABLE ESTADO_REPORTE (
                                ID_ESTADO      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                NOMBRE_ESTADO  VARCHAR2(30) NOT NULL UNIQUE
)
CREATE TABLE PROGRAMA_EDUCATIVO (
                                    ID_PROGRAMA        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    ID_SOLICITUD       NUMBER NOT NULL,
                                    DIVISION_ACADEMICA VARCHAR2(100) NOT NULL,
                                    CUATRIMESTRE       NUMBER(2) NOT NULL,
                                    GRUPO              VARCHAR2(10),
                                    NO_ESTUDIANTES     NUMBER NOT NULL,
                                    CONSTRAINT FK_PROGRAMA_SOLICITUD FOREIGN KEY (ID_SOLICITUD)
                                        REFERENCES SOLICITUD (ID_SOLICITUD)
)
CREATE TABLE SOLICITUD_DOCENTE (
                                   ID_SOLICITUD  NUMBER NOT NULL,
                                   ID_USUARIO    NUMBER NOT NULL,
                                   CONSTRAINT PK_SOLICITUD_DOCENTE PRIMARY KEY (ID_SOLICITUD, ID_USUARIO),
                                   CONSTRAINT FK_SOLDOC_SOLICITUD FOREIGN KEY (ID_SOLICITUD)
                                       REFERENCES SOLICITUD (ID_SOLICITUD),
                                   CONSTRAINT FK_SOLDOC_USUARIO FOREIGN KEY (ID_USUARIO)
                                       REFERENCES USUARIO (ID_USUARIO)
)
CREATE TABLE ASIGNATURA_REFORZAR_SOLICITUD (
                                               ID_ASIGNATURA  NUMBER GENERATED ALWAYS AS IDENTITY,
                                               ID_SOLICITUD   NUMBER NOT NULL,
                                               NOMBRE         VARCHAR2(100) NOT NULL,

                                               CONSTRAINT PK_ASIGNATURA_REFORZAR PRIMARY KEY (ID_ASIGNATURA),
                                               CONSTRAINT FK_ASIGNATURA_SOLICITUD FOREIGN KEY (ID_SOLICITUD)
                                                   REFERENCES SOLICITUD (ID_SOLICITUD)
)
CREATE TABLE SOLICITUD (
                           ID_SOLICITUD              NUMBER GENERATED ALWAYS AS IDENTITY,
                           ID_USUARIO_SOLICITANTE     NUMBER NOT NULL,
                           ID_USUARIO_AUTORIZA        NUMBER,
                           NOMBRE_EMPRESA_ACTIVIDAD   VARCHAR2(150) NOT NULL,
                           LUGAR_DIRECCION            VARCHAR2(200),
                           TELEFONO_CONTACTO          VARCHAR2(20),
                           CORREO_CONTACTO            VARCHAR2(100),
                           FECHA_INICIO               DATE,
                           OBJETIVO                   VARCHAR2(500),
                           AREA_SOLICITANTE           VARCHAR2(100),
                           ID_ESTADO                  NUMBER NOT NULL,
                           DETALLES_DECISION          VARCHAR2(500),
                           FECHA_CREACION             DATE DEFAULT SYSDATE NOT NULL,

                           CONSTRAINT PK_SOLICITUD PRIMARY KEY (ID_SOLICITUD),
                           CONSTRAINT FK_SOLICITUD_USR_SOLICITANTE FOREIGN KEY (ID_USUARIO_SOLICITANTE)
                               REFERENCES USUARIO (ID_USUARIO),
                           CONSTRAINT FK_SOLICITUD_USR_AUTORIZA FOREIGN KEY (ID_USUARIO_AUTORIZA)
                               REFERENCES USUARIO (ID_USUARIO),
                           CONSTRAINT FK_SOLICITUD_ESTADO FOREIGN KEY (ID_ESTADO)
                               REFERENCES ESTADO_SOLICITUD (ID_ESTADO)
)
CREATE TABLE ESTADO_SOLICITUD (
                                  ID_ESTADO      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  NOMBRE_ESTADO  VARCHAR2(30) NOT NULL UNIQUE
)
CREATE TABLE CONTRASENA (
                            ID_USUARIO           NUMBER PRIMARY KEY,
                            HASH_PASSWORD        VARCHAR2(255) NOT NULL,
                            FECHA_ACTUALIZACION  DATE DEFAULT SYSDATE,
                            CONSTRAINT FK_CONTRASENA_USUARIO FOREIGN KEY (ID_USUARIO)
                                REFERENCES USUARIO (ID_USUARIO)
)
CREATE TABLE USUARIO (
                         ID_USUARIO   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         ID_ROL       NUMBER NOT NULL,
                         NOMBRE       VARCHAR2(100) NOT NULL,
                         CORREO       VARCHAR2(100) NOT NULL UNIQUE,
                         CONSTRAINT FK_USUARIO_ROL FOREIGN KEY (ID_ROL)
                             REFERENCES ROL (ID_ROL)
)
CREATE TABLE Rol (
                     id_rol     NUMBER GENERATED ALWAYS AS IDENTITY,
                     nombre_rol VARCHAR2(30) NOT NULL,
                     CONSTRAINT pk_rol PRIMARY KEY (id_rol),
                     CONSTRAINT uq_nombre_rol UNIQUE (nombre_rol)
)
DROP TABLE VISITAS