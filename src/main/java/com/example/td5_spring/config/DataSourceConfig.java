package com.example.td5_spring.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceConfig {
    private static final String URL = "jdbc:postgresql://localhost:5432/mini_dish_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "aina";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}