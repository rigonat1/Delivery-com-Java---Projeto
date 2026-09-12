package com.delivery.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centraliza a criação de conexões com o PostgreSQL.
 * Lê os dados de conexão de src/main/resources/config.properties,
 * para que usuário/senha/URL nunca fiquem "hardcoded" no código.
 */
public class ConnectionFactory {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConnectionFactory.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Aviso: não foi possível ler config.properties (" + e.getMessage() + ")");
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/delivery_db");
        String user = props.getProperty("db.user", "postgres");
        String password = props.getProperty("db.password", "postgres");
        return DriverManager.getConnection(url, user, password);
    }
}
