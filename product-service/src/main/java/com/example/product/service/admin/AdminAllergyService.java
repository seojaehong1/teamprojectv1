package com.example.product.service.admin;

import com.example.product.dto.admin.AllergySelectDto;
import com.example.product.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAllergyService {

    private final AllergyRepository allergyRepository;

    public List<AllergySelectDto> getAllAllergies() {
        return allergyRepository.findAll().stream()
                .map(a -> AllergySelectDto.builder()
                        .allergyId(a.getAllergyId())
                        .allergyName(a.getAllergyName())
                        .build()
                )
                .toList();
    }
}
