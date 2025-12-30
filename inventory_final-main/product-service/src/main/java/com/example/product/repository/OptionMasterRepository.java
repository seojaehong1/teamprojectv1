package com.example.product.repository;

import com.example.product.model.OptionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OptionMasterRepository extends JpaRepository<OptionMaster, Integer> {
    @Query("SELECT o FROM OptionMaster o WHERE o.optionGroupName IN :groups")
    List<OptionMaster> findByOptionGroupNames(@Param("groups") List<String> groups);
}