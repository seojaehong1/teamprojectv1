package com.du.adminservice.repository;

import com.du.adminservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, String> {

    // 카테고리별 조회
    Page<Product> findByCategory(String category, Pageable pageable);

    // 검색 기능 (상품명, 카테고리로 검색)
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR " +
           "p.menuName LIKE %:keyword% OR " +
           "p.category LIKE %:keyword%)")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리별 검색
    @Query("SELECT p FROM Product p WHERE p.category = :category AND " +
           "(:keyword IS NULL OR p.menuName LIKE %:keyword%)")
    Page<Product> searchProductsByCategory(@Param("category") String category,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    // 판매 상태별 조회
    Page<Product> findByIsAvailable(Boolean isAvailable, Pageable pageable);
}
