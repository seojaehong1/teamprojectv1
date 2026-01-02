package com.example.product.service;

import com.example.product.dto.user.OptionGroupDto;
import com.example.product.dto.user.OptionValueDto;
import com.example.product.model.*;
import com.example.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionService {

    private final MenuOptionRepository menuOptionRepository;
    private final OptionMasterRepository optionMasterRepository;

    //옵션 조회 [GET /api/menu/options?menuCode=?]
    public List<OptionGroupDto> getOptionsByMenu(Integer menuCode) {

        // 1. 메뉴에 연결된 옵션 그룹 조회
        List<MenuOption> menuOptions =
                menuOptionRepository.findByMenu_MenuCode(menuCode);

        if (menuOptions.isEmpty()) {
            return List.of();
        }

        List<String> optionGroupNames = menuOptions.stream()
                .map(MenuOption::getOptionGroupName)
                .distinct()
                .toList();

        // 2. 옵션 마스터 조회
        List<OptionMaster> optionMasters =
                optionMasterRepository.findByOptionGroupNameIn(optionGroupNames);

        // 3. 그룹별로 묶기
        Map<String, List<OptionMaster>> grouped =
                optionMasters.stream()
                        .collect(Collectors.groupingBy(OptionMaster::getOptionGroupName));

        // 4. DTO 변환
        return grouped.entrySet().stream()
                .map(entry -> OptionGroupDto.builder()
                        .optionGroupName(entry.getKey())
                        .options(
                                entry.getValue().stream()
                                        .map(option -> OptionValueDto.builder()
                                                .optionId(option.getOptionId())
                                                .optionName(option.getOptionName())
                                                .price(option.getDefaultPrice())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }
}
