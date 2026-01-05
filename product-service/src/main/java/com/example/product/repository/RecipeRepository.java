package com.example.product.repository;

import com.example.product.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByMenu_MenuCode(Long menuCode);
    void deleteByMenu_MenuCode(Long menuCode);
}