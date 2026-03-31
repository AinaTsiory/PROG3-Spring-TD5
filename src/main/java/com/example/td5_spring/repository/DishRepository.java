package com.example.td5_spring.repository;

import com.example.td5_spring.config.DataSourceConfig;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    }
}