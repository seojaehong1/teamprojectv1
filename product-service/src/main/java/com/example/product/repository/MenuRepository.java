package com.example.product.repository;

import com.example.product.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaRepository의 두 번째 인자는 PK의 타입(String)으로 변경
public interface MenuRepository extends JpaRepository<Menu, String> {

    // 모든 메뉴 목록을 카테고리별로 정렬하여 조회
    List<Menu> findAllByOrderByCategoryAscMenuNameAsc();

    // 네비게이션을 위한 모든 카테고리 목록 조회
    @Query("SELECT DISTINCT m.category FROM Menu m ORDER BY m.category")
    List<String> findAllCategories();

    // 특정 카테고리의 메뉴 목록 조회
    List<Menu> findByCategory(String category);
}