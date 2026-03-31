package com.example.td5_spring.repository;

import com.example.td5_spring.entity.Ingredient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IngredientRepository {
    private final JdbcTemplate jdbc;
    public IngredientRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Ingredient> findAll() {
        return jdbc.query("SELECT * FROM ingredient", (rs, row) ->
                new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getString("category")));
    }

    public Ingredient findById(Integer id) {
        return jdbc.queryForObject("SELECT * FROM ingredient WHERE id = ?", (rs, row) ->
                new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getString("category")), id);
    }
}