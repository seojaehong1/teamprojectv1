package com.example.product.repository;

import com.example.product.model.MaterialMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialMasterRepository
        extends JpaRepository<MaterialMaster, Integer> {
}
