package com.example.product.repository;

import com.example.product.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 1. 전체 메뉴 조회
    Page<Menu> findAll(Pageable pageable);
    // 2. 카테고리별 메뉴 조회
    Page<Menu> findByCategory(String category, Pageable pageable);
    // 3. 메뉴명 검색
    Page<Menu> findByMenuNameContaining(String keyword, Pageable pageable);
}
