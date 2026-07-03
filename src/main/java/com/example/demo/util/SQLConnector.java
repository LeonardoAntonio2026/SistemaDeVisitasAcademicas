package com.example.demo.util;

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
            String dbUser = System.getenv("db_user");
            String dbPass = System.getenv("db_pass");
            String dbName = System.getenv("db_name");

            // Si falta alguno en el entorno, buscamos en el archivo .properties real
            if (dbUser == null || dbPass == null || dbName == null) {
                System.err.println("Advertencia: Faltan variables de entorno de la BD. Buscando en credentials.properties...");
                Properties creds = new Properties();
                // 👇 CORREGIDO: Buscamos "credentials.properties" directamente
                try (InputStream is = classLoader.getResourceAsStream("credentials.properties")) {
                    if (is == null) {
                        throw new RuntimeException("No se encontró el archivo credentials.properties ni las variables de entorno.");
                    }
                    creds.load(is);

                    if (dbUser == null) dbUser = creds.getProperty("db_user");
                    if (dbPass == null) dbPass = creds.getProperty("db_pass");
                    if (dbName == null) dbName = creds.getProperty("db_name");
                }
            }

            if (dbName == null) {
                throw new RuntimeException("El nombre de la base de datos no está configurado.");
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("oracle.jdbc.OracleDriver");
            config.setJdbcUrl("jdbc:oracle:thin:@" + dbName + "?TNS_ADMIN=" + walletPath);

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
            System.out.println("¡Pool de conexiones inicializado con éxito!");

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
}