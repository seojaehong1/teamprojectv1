package com.example.product.repository;

import com.example.product.model.OptionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionMasterRepository extends JpaRepository<OptionMaster, Integer> {
    List<OptionMaster> findByOptionGroupNameIn(List<String> optionGroupNames);
}
