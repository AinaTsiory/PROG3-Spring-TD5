package com.example.td5_spring.repository;

import com.example.td5_spring.entity.Dish;
import com.example.td5_spring.entity.Model;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DishRepository {
    private final JdbcTemplate jdbc;
    public DishRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Dish findById(Integer id) {
        return jdbc.queryForObject("SELECT id, name FROM dish WHERE id = ?", (rs, row) -> {
            Dish d = new Dish();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));
            return d;
        }, id);
    }

    public void updateAssociations(Integer dishId, List<Integer> ingredientIds) {
        jdbc.update("DELETE FROM dish_ingredient WHERE id_dish = ?", dishId);
        for (Integer ingId : ingredientIds) {
            jdbc.update("INSERT INTO dish_ingredient (id_dish, id_ingredient) VALUES (?, ?)", dishId, ingId);
        }
    }
}
