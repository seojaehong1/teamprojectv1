package com.example.product.service;



import com.example.product.dto.OptionDto;
import com.example.product.model.Menu;
import com.example.product.model.MenuOption;
import com.example.product.model.OptionMaster;
import com.example.product.repository.MenuOptionRepository;
import com.example.product.repository.MenuRepository;
import com.example.product.repository.OptionMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final OptionMasterRepository optionMasterRepository;
    private final MenuOptionRepository menuOptionRepository;

    /**
     * 모든 메뉴 항목을 조회합니다.
     */
    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByCategoryAscMenuNameAsc();
    }

    /**
     * 네비게이션에 사용할 모든 고유 카테고리 목록을 조회합니다.
     */
    public List<String> getAllCategories() {
        // DB에서 조회된 카테고리 목록을 반환
        return menuRepository.findAllCategories();
    }

    public List<Menu> getMenusByCategory(String category) {
        return menuRepository.findByCategory(category);
    }

    /**
     * ID(menuCode)로 메뉴 조회
     */
    public Optional<Menu> getMenuById(String menuCode) {
        return menuRepository.findById(menuCode);
    }

    public List<OptionDto> getAllOptions() {

        // 1. DB에서 모든 OptionMaster Entity를 조회합니다.
        List<OptionMaster> entities = optionMasterRepository.findAll();

        // 2. Entity 리스트를 Stream으로 변환하고, 각 Entity의 toDto() 메서드를 호출하여 OptionDto로 매핑합니다.
        return entities.stream()
                .map(OptionMaster::toDto) // OptionMaster Entity에 정의된 toDto() 메서드 사용
                .collect(Collectors.toList());
    }

    public List<OptionDto> getOptionsByMenuCode(String menuCode) {

        // 1. menu_option 테이블에서 해당 메뉴 코드에 허용된 옵션 그룹 이름 목록을 조회
        List<MenuOption> menuOptions = menuOptionRepository.findByMenuCode(menuCode);

        Set<String> allowedGroupNames = menuOptions.stream()
                .map(MenuOption::getOptionGroupName)
                .collect(Collectors.toSet());

        if (allowedGroupNames.isEmpty()) {
            return List.of(); // 허용된 옵션 그룹이 없으면 빈 리스트 반환
        }

        // 2. option_master 테이블에서 허용된 옵션 그룹에 속하는 모든 상세 옵션을 조회
        // (주의: optionMasterRepository에 optionGroupName을 기준으로 조회하는 메서드가 필요합니다.)
        List<OptionMaster> masterOptions = optionMasterRepository.findByOptionGroupNameIn(allowedGroupNames);

        // 3. OptionMaster Entity 리스트를 OptionDto로 변환하여 반환
        return masterOptions.stream()
                .map(OptionMaster::toDto)
                .collect(Collectors.toList());
    }
}