package com.example.product.service.admin;

import com.example.product.dto.admin.CategoriSelectDto;
import com.example.product.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoriService {

    private final MenuRepository menuRepository;

    public List<CategoriSelectDto> getAllCategories() {
        return menuRepository.findAll().stream()
                .map(menu -> menu.getCategory())
                .filter(category -> category != null && !category.isBlank())
                .distinct() // 중복 제거
                .map(CategoriSelectDto::new) // 생성자로 DTO 변환
                .collect(Collectors.toList());
    }
}