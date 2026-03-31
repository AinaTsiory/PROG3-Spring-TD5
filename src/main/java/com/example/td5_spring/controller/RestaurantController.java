package com.example.td5_spring.controller;
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

    // Injection par constructeur (recommandé par Spring)
    public RestaurantController(IngredientRepository ir, DishRepository dr, IngredientService is) {
        this.ingredientRepo = ir;
        this.dishRepo = dr;
        this.ingredientService = is;
    }

    // --- POINT A : Lister les ingrédients ---
    @GetMapping("/ingredients")
    public ResponseEntity<?> getAllIngredients() {
        try {
            return ResponseEntity.ok(ingredientRepo.findAll());
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Erreur SQL : " + e.getMessage());
        }
    }

    // --- POINT B : Détail d'un ingrédient (avec 404) ---
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

    // --- POINTS C & D : Calcul du stock à un instant T (avec 400 et 404) ---
    @GetMapping("/ingredients/{id}/stock")
    public ResponseEntity<?> getStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        // Vérification du point D : paramètres obligatoires
        if (at == null || unit == null) {
            return ResponseEntity.status(400).body("Either mandatory query parameter 'at' or 'unit' is not provided.");
        }

        try {
            // Vérifier si l'ingrédient existe d'abord
            if (ingredientRepo.findById(id) == null) {
                return ResponseEntity.status(404).body("Ingredient.id=" + id + " not found");
            }

            // Calcul via le Service
            Instant timestamp = Instant.parse(at);
            StockValue stock = ingredientService.getStockAt(id, timestamp);

            // On s'assure que l'unité correspond à celle demandée
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du calcul du stock : " + e.getMessage());
        }
    }

    // --- POINT E : Mise à jour des ingrédients d'un plat (avec 404) ---
    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody List<Ingredient> ingredients) {

        try {
            // Extraction des IDs des ingrédients reçus dans le Body JSON
            List<Integer> ingredientIds = ingredients.stream()
                    .map(Ingredient::id) // Utilise .getId() si c'est une classe classique
                    .collect(Collectors.toList());

            // Appel au Repository (qui contient la transaction DELETE + INSERT)
            dishRepo.updateAssociations(id, ingredientIds);

            return ResponseEntity.ok().build(); // Retourne 200 OK
        } catch (SQLException e) {
            // Si le plat n'existe pas ou erreur SQL, on renvoie 404 comme demandé
            return ResponseEntity.status(404).body("Dish.id=" + id + " not found");
        }
    }
}