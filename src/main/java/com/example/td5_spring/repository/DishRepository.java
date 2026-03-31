package com.example.td5_spring.repository;

import com.example.td5_spring.config.DataSourceConfig;
import com.example.td5_spring.entity.Dish;
import com.example.td5_spring.entity.DishCreateDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Repository
public class DishRepository {

    public void updateAssociations(Integer dishId, List<Integer> ingredientIds) throws SQLException {
        String deleteSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        String insertSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient) VALUES (?, ?)";

        try (Connection conn = DataSourceConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                deleteStmt.setInt(1, dishId);
                deleteStmt.executeUpdate();

                if (ingredientIds != null && !ingredientIds.isEmpty()) {
                    for (Integer ingId : ingredientIds) {
                        insertStmt.setInt(1, dishId);
                        insertStmt.setInt(2, ingId);
                        insertStmt.addBatch(); // On prépare l'envoi groupé
                    }
                    insertStmt.executeBatch(); // On envoie tout d'un coup pour la performance
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }public List<Dish> saveAll(List<DishCreateDTO> dtos) throws SQLException {
        List<Dish> createdDishes = new ArrayList<>();
        String checkSql = "SELECT COUNT(*) FROM dish WHERE name = ?";
        // Utilisation de selling_price et du cast vers dish_type
        String insertSql = "INSERT INTO dish (name, dish_type, selling_price) " +
                "VALUES (?, ?::text::dish_type, ?) RETURNING id";

        try (Connection conn = DataSourceConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (DishCreateDTO dto : dtos) {
                    // 1. Vérification doublon
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, dto.name());
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new IllegalArgumentException("Dish.name=" + dto.name() + " already exists");
                        }
                    }

                    // 2. Insertion
                    try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                        insStmt.setString(1, dto.name());
                        insStmt.setString(2, dto.category());

                        if (dto.price() != null) {
                            insStmt.setDouble(3, dto.price());
                        } else {
                            insStmt.setNull(3, java.sql.Types.DOUBLE);
                        }

                        try (ResultSet rs = insStmt.executeQuery()) {
                            if (rs.next()) {
                                createdDishes.add(new Dish(
                                        rs.getInt(1),
                                        dto.name(),
                                        dto.category(),
                                        dto.price()
                                ));
                            }
                        }
                    }
                }
                conn.commit();
                return createdDishes;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // --- GET /dishes : RECHERCHE AVEC FILTRES ---
    public List<Dish> findByFilters(Double priceUnder, Double priceOver, String name) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, name, dish_type, selling_price FROM dish WHERE 1=1");

        if (priceUnder != null) sql.append(" AND selling_price <= ?");
        if (priceOver != null)  sql.append(" AND selling_price >= ?");
        if (name != null)       sql.append(" AND name ILIKE ?");

        try (Connection conn = DataSourceConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (priceUnder != null) pstmt.setDouble(paramIndex++, priceUnder);
            if (priceOver != null)  pstmt.setDouble(paramIndex++, priceOver);
            if (name != null)       pstmt.setString(paramIndex++, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("id");
                    String dishName = rs.getString("name"); // Changé pour éviter le conflit de scope
                    String category = rs.getString("dish_type");

                    Double priceValue = null;
                    double p = rs.getDouble("selling_price");
                    if (!rs.wasNull()) {
                        priceValue = p;
                    }

                    dishes.add(new Dish(id, dishName, category, priceValue));                }
            }
        }
        return dishes;
    }
}