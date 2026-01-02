package com.example.product.service.admin;

import com.example.product.dto.admin.OptionGroupDto;
import com.example.product.dto.admin.OptionItemDto;
import com.example.product.model.OptionMaster;
import com.example.product.repository.OptionMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOptionService {

    private final OptionMasterRepository optionMasterRepository;

    // 옵션 전체 조회 (그룹별) 메뉴 등록/수정 화면용
    public List<OptionGroupDto> getAllOptionsGrouped() {

        List<OptionMaster> optionMasters = optionMasterRepository.findAll();

        if (optionMasters.isEmpty()) {
            return List.of();
        }

        // 1. 그룹별로 묶기
        Map<String, List<OptionMaster>> grouped =
                optionMasters.stream()
                        .collect(Collectors.groupingBy(
                                OptionMaster::getOptionGroupName
                        ));

        // 2. DTO 변환
        return grouped.entrySet().stream()
                .map(entry -> OptionGroupDto.builder()
                        .optionGroupName(entry.getKey())
                        .options(
                                entry.getValue().stream()
                                        .map(option -> OptionItemDto.builder()
                                                .optionId(option.getOptionId())
                                                .optionName(option.getOptionName())
                                                .defaultPrice(option.getDefaultPrice())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }
}
