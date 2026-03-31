package com.example.td5_spring.controller;
import com.example.td5_spring.entity.Ingredient;
import com.example.td5_spring.entity.StockValue;
import com.example.td5_spring.repository.DishRepository;
import com.example.td5_spring.repository.IngredientRepository;
import com.example.td5_spring.service.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/")
public class RestaurantController {

    private final IngredientRepository ingredientRepo;
    private final DishRepository dishRepo;
    private final IngredientService ingredientService;

    public RestaurantController(IngredientRepository ir, DishRepository dr, IngredientService is) {
        this.ingredientRepo = ir;
        this.dishRepo = dr;
        this.ingredientService = is;
    }

    // a) GET /ingredients
    @GetMapping("/ingredients")
    public List<Ingredient> getAllIngredients() {
        return ingredientRepo.findAll();
    }

    // b) GET /ingredients/{id}
    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredient(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ingredientRepo.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Ingredient.id=" + id + " not found");
        }
    }

    // c) & d) GET /ingredients/{id}/stock
    @GetMapping("/ingredients/{id}/stock")
    public ResponseEntity<?> getStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity.status(400).body("Either mandatory query parameter 'at' or 'unit' is not provided.");
        }

        try {
            ingredientRepo.findById(id); // Vérifie 404
            StockValue sv = ingredientService.getStockAt(id, Instant.parse(at));
            return ResponseEntity.ok(sv);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Ingredient.id=" + id + " not found");
        }
    }

    // e) PUT /dishes/{id}/ingredients
    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDish(@PathVariable Integer id, @RequestBody List<Ingredient> ingredients) {
        try {
            dishRepo.findById(id); // Vérifie 404
            List<Integer> ids = ingredients.stream().map(Ingredient::id).toList();
            dishRepo.updateAssociations(id, ids);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Dish.id=" + id + " not found");
        }
    }
}
