package com.example.demo.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnector {

    private static HikariDataSource dataSource;

    static {
        try {
            // 1. Localizar mis conjuntos de Wallet
            ClassLoader classLoader = SQLConnector.class.getClassLoader();
            URL walletUrl = classLoader.getResource("wallet/");

            if (walletUrl == null) {
                throw new RuntimeException("No se encontró la Wallet");
            }

            String walletPath = new File(walletUrl.toURI()).getAbsolutePath();
            walletPath = walletPath.replace("\\", "/");

            // 2. Intentar leer credenciales y nombre de BD desde el entorno
            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");
            String dbName = System.getenv("DB_NAME");

            // Si falta alguno en el entorno, buscamos en el archivo .properties
            if (dbUser == null || dbPass == null || dbName == null) {
                System.err.println("Advertencia: Faltan variables de entorno de la BD. Buscando en credentials.properties...");
                Properties creds = new Properties();

                try (InputStream is = classLoader.getResourceAsStream("credentials.properties")) {
                    if (is == null) {
                        throw new RuntimeException("No se encontró el archivo credentials.properties ni las variables de entorno de la base de datos.");
                    }

                    // 1. Convertimos el InputStream en bytes para poder analizarlo sin consumirlo
                    byte[] fileBytes = is.readAllBytes();

                    // 2. Detectamos dinámicamente la codificación
                    java.nio.charset.Charset detectedCharset = detectCharset(fileBytes);
                    System.out.println("Codificación detectada para credentials.properties: " + detectedCharset.name());

                    // 3. Cargamos las propiedades usando el encoding correcto
                    try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileBytes);
                         java.io.InputStreamReader reader = new java.io.InputStreamReader(bais, detectedCharset)) {
                        creds.load(reader);
                    }

                    // Si ya se habían leído del entorno, conservamos ese valor; si no, del archivo
                    if (dbUser == null) dbUser = creds.getProperty("db.user");
                    if (dbPass == null) dbPass = creds.getProperty("db.pass");
                    if (dbName == null) dbName = creds.getProperty("db.name");
                }
            }

            // Validar que finalmente tengamos el nombre de la BD
            if (dbName == null) {
                throw new RuntimeException("El nombre de la base de datos (db.name / DB_NAME) no está configurado.");
            }

            // Configuración de Hikari (incluye conf de conexion y pool)
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("oracle.jdbc.OracleDriver");

            // Concatenamos la variable dbName dinámicamente aquí:
            config.setJdbcUrl("jdbc:oracle:thin:@" + dbName + "?TNS_ADMIN=" + walletPath);

            // Asignar los valores dinámicos y seguros
            config.setUsername(dbUser);
            config.setPassword(dbPass);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(20000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            System.out.println("¡Pool de conexiones a la base de datos inicializado con éxito!");

        } catch (Exception e) {
            System.err.println("Error crítico al inicializar la base de datos");
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection() {
        if(dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Detecta si un arreglo de bytes pertenece a un archivo UTF-8 o ISO-8859-1.
     */
    private static java.nio.charset.Charset detectCharset(byte[] bytes) {
        // 1. Revisar si tiene BOM de UTF-8 (tres bytes específicos al inicio: EF BB BF)
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return java.nio.charset.StandardCharsets.UTF_8;
        }

        // 2. Intentar decodificar como UTF-8 estricto.
        // Si contiene caracteres ISO-8859-1 puros (como la 'ñ' en un solo byte), fallará.
        java.nio.charset.CharsetDecoder decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder();
        decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);

        try {
            decoder.decode(java.nio.ByteBuffer.wrap(bytes));
            return java.nio.charset.StandardCharsets.UTF_8; // Si no falló, es UTF-8 válido (o texto plano ASCII)
        } catch (java.nio.charset.CharacterCodingException e) {
            // Si lanza excepción, significa que encontró bytes inválidos para UTF-8 (como tu 'ñ' en ISO)
            return java.nio.charset.StandardCharsets.ISO_8859_1;
        }
    }
}