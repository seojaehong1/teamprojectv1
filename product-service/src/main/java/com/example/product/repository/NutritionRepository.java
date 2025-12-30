package com.example.product.repository;


import com.example.product.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {

    /**
     * 메뉴 ID로 영양 정보 조회 (일반적으로 메뉴는 하나의 영양 정보만 가집니다.)
     */
    Optional<Nutrition> findByMenuCode(String menuCode);

    /**
     * 여러 메뉴 코드에 대한 영양 정보 일괄 조회
     */
    List<Nutrition> findByMenuCodeIn(Collection<String> menuCodes);
}