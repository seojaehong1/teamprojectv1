package com.example.product.config;

import com.example.product.model.Menu;
import com.example.product.repository.MenuRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataInitializer {

    private final MenuRepository menuRepository;

    public DataInitializer(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (menuRepository.count() == 0) {
            // 커피 카테고리
            menuRepository.save(Menu.builder()
                    .menuCode("COF-001")
                    .menuName("아메리카노")
                    .basePrice(new BigDecimal("4000"))
                    .category("커피")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("COF-002")
                    .menuName("카페라떼")
                    .basePrice(new BigDecimal("4500"))
                    .category("커피")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("COF-003")
                    .menuName("카푸치노")
                    .basePrice(new BigDecimal("4500"))
                    .category("커피")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("COF-004")
                    .menuName("카라멜 마키아토")
                    .basePrice(new BigDecimal("5000"))
                    .category("커피")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("COF-005")
                    .menuName("바닐라라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("커피")
                    .build());

            // 라떼 카테고리
            menuRepository.save(Menu.builder()
                    .menuCode("LAT-001")
                    .menuName("그린티라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("라떼")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("LAT-002")
                    .menuName("고구마라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("라떼")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("LAT-003")
                    .menuName("초콜릿라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("라떼")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("LAT-004")
                    .menuName("고구마라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("라떼")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("LAT-005")
                    .menuName("고구마라떼")
                    .basePrice(new BigDecimal("5000"))
                    .category("라떼")
                    .build());

            // 에이드 카테고리
            menuRepository.save(Menu.builder()
                    .menuCode("ADE-001")
                    .menuName("레몬에이드")
                    .basePrice(new BigDecimal("5500"))
                    .category("에이드")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("ADE-002")
                    .menuName("자몽에이드")
                    .basePrice(new BigDecimal("5500"))
                    .category("에이드")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("ADE-003")
                    .menuName("청포도에이드")
                    .basePrice(new BigDecimal("5500"))
                    .category("에이드")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("ADE-004")
                    .menuName("유자에이드")
                    .basePrice(new BigDecimal("5500"))
                    .category("에이드")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("ADE-005")
                    .menuName("패션후르츠에이드")
                    .basePrice(new BigDecimal("5500"))
                    .category("에이드")
                    .build());

            // 티 카테고리
            menuRepository.save(Menu.builder()
                    .menuCode("T-001")
                    .menuName("얼그레이티")
                    .basePrice(new BigDecimal("4000"))
                    .category("티")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("T-002")
                    .menuName("캐모마일티")
                    .basePrice(new BigDecimal("4000"))
                    .category("티")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("T-003")
                    .menuName("페퍼민트티")
                    .basePrice(new BigDecimal("4000"))
                    .category("티")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("T-004")
                    .menuName("유자차")
                    .basePrice(new BigDecimal("4500"))
                    .category("티")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("T-005")
                    .menuName("생강차")
                    .basePrice(new BigDecimal("4500"))
                    .category("티")
                    .build());

            // 스무디 카테고리
            menuRepository.save(Menu.builder()
                    .menuCode("SMD-001")
                    .menuName("딸기스무디")
                    .basePrice(new BigDecimal("6000"))
                    .category("스무디")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("SMD-002")
                    .menuName("망고스무디")
                    .basePrice(new BigDecimal("6000"))
                    .category("스무디")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("SMD-003")
                    .menuName("블루베리스무디")
                    .basePrice(new BigDecimal("6000"))
                    .category("스무디")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("SMD-004")
                    .menuName("요거트스무디")
                    .basePrice(new BigDecimal("6000"))
                    .category("스무디")
                    .build());
            
            menuRepository.save(Menu.builder()
                    .menuCode("SMD-005")
                    .menuName("초코스무디")
                    .basePrice(new BigDecimal("6000"))
                    .category("스무디")
                    .build());
        }
    }
}
