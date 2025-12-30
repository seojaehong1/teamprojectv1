package com.example.product.repository;


import com.example.product.model.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
    // 특정 menu_code에 해당하는 모든 옵션 그룹을 조회합니다.
    List<MenuOption> findByMenuCode(String menuCode);
}