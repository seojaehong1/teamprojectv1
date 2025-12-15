package com.example.product.repository;


import com.example.product.model.OptionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OptionMasterRepository extends JpaRepository<OptionMaster, Long> {

    /**
     * 옵션 이름(그룹)으로 옵션 상세 목록 조회 (예: "사이즈" 그룹의 모든 옵션 조회)
     */
    List<OptionMaster> findByOptionName(String optionName);
    List<OptionMaster> findByOptionGroupName(String optionGroupName);

    List<OptionMaster> findByOptionGroupNameIn(Collection<String> optionGroupNames);
}