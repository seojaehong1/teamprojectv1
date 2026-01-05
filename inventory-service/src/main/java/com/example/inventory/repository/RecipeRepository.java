package com.example.inventory.repository;

import com.example.inventory.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByMenuCode(String menuCode);
}
