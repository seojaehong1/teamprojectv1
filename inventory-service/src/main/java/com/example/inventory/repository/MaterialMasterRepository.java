package com.example.inventory.repository;

import com.example.inventory.model.MaterialMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialMasterRepository extends JpaRepository<MaterialMaster,Integer> {
    Optional<MaterialMaster> findByIngredientName(String ingredientName);
    List<MaterialMaster> findByIngredientNameIn(List<String> ingredientNames);
}
