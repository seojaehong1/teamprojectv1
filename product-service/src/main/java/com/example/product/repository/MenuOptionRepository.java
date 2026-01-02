package com.example.product.repository;

import com.example.product.model.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
    List<MenuOption> findByMenu_MenuCode(Integer menuCode);
    void deleteByMenu_MenuCode(Integer menuCode);
}
