package com.example.td5_spring.entity;

public class Dish {
    private Integer id;
    private String name;
    private String category;
    private Double price;

    public Dish(Integer id, String name, String category, Double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    // GETTERS : INDISPENSABLES POUR POSTMAN
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    // SETTERS (Optionnels mais conseillés)
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(Double price) { this.price = price; }
}