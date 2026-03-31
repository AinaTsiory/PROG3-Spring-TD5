package com.example.td5_spring.service;

import com.example.td5_spring.entity.StockValue;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class IngredientService {
    private final JdbcTemplate jdbc;
    public IngredientService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public StockValue getStockAt(Integer ingredientId, Instant t) {
        String sql = """
            SELECT unit, SUM(CASE WHEN type = 'IN' THEN quantity ELSE -quantity END) as actual_quantity
            FROM stock_movement WHERE id_ingredient = ? AND creation_datetime <= ?
            GROUP BY unit
        """;
        return jdbc.query(sql, rs -> {
            if (rs.next()) return new StockValue(rs.getDouble("actual_quantity"), rs.getString("unit"));
            return new StockValue(0.0, "kg");
        }, ingredientId, java.sql.Timestamp.from(t));
    }
}