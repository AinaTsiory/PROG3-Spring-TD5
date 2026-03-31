package com.example.td5_spring.controller;
import com.example.td5_spring.entity.Dish;
import com.example.td5_spring.entity.DishCreateDTO;
import com.example.td5_spring.entity.Ingredient;
import com.example.td5_spring.entity.StockValue;
import com.example.td5_spring.repository.DishRepository;
import com.example.td5_spring.repository.IngredientRepository;
import com.example.td5_spring.service.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/ingredients")
    public ResponseEntity<?> getAllIngredients() {
        try {
            return ResponseEntity.ok(ingredientRepo.findAll());
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Erreur SQL : " + e.getMessage());
        }
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable Integer id) {
        try {
            Ingredient ing = ingredientRepo.findById(id);
            if (ing == null) {
                return ResponseEntity.status(404).body("Ingredient.id=" + id + " not found");
            }
            return ResponseEntity.ok(ing);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }

    @GetMapping("/ingredients/{id}/stock")
    public ResponseEntity<?> getStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity.status(400).body("Either mandatory query parameter 'at' or 'unit' is not provided.");
        }

        try {
            if (ingredientRepo.findById(id) == null) {
                return ResponseEntity.status(404).body("Ingredient.id=" + id + " not found");
            }

            Instant timestamp = Instant.parse(at);
            StockValue stock = ingredientService.getStockAt(id, timestamp);

            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du calcul du stock : " + e.getMessage());
        }
    }

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody List<Ingredient> ingredients) {

        try {
            List<Integer> ingredientIds = ingredients.stream()
                    .map(Ingredient::id) // Utilise .getId() si c'est une classe classique
                    .collect(Collectors.toList());

            dishRepo.updateAssociations(id, ingredientIds);

            return ResponseEntity.ok().build(); // Retourne 200 OK
        } catch (SQLException e) {
            return ResponseEntity.status(404).body("Dish.id=" + id + " not found");
        }
    }
    @PostMapping("/dishes")
    public ResponseEntity<?> createDishes(@RequestBody List<DishCreateDTO> dishDTOs) {
        try {
            List<Dish> created = dishRepo.saveAll(dishDTOs);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/dishes")
    public ResponseEntity<?> getDishes(
            @RequestParam(required = false) Double priceUnder,
            @RequestParam(required = false) Double priceOver,
            @RequestParam(required = false) String name) {
        try {
            List<Dish> filteredDishes = dishRepo.findByFilters(priceUnder, priceOver, name);
            return ResponseEntity.ok(filteredDishes);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}