package com.example.td5_spring.service;

import com.example.td5_spring.config.DataSourceConfig;
import com.example.td5_spring.entity.StockValue;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Instant;

@Service
public class IngredientService {

    public StockValue getStockAt(Integer ingredientId, Instant t) throws SQLException {
        String sql = "SELECT unit, SUM(CASE WHEN type = 'IN' THEN quantity ELSE -quantity END) as actual_qty " +
                "FROM stock_movement WHERE id_ingredient = ? AND creation_datetime <= ? " +
                "GROUP BY unit";

        try (Connection conn = DataSourceConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ingredientId);
            pstmt.setTimestamp(2, Timestamp.from(t));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new StockValue(rs.getDouble("actual_qty"), rs.getString("unit"));
                }
            }
        }
        return new StockValue(0.0, "kg"); // Valeur par défaut
    }
}