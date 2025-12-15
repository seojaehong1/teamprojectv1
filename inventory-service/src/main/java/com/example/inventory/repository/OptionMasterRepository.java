package com.example.inventory.repository;

import com.example.inventory.model.OptionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionMasterRepository extends JpaRepository<OptionMaster, Integer> {
    List<OptionMaster> findByOptionIdIn(List<Integer> optionIds);
}
