package com.example.product.service.admin;

import com.example.product.dto.admin.MaterialSelectDto;
import com.example.product.repository.MaterialMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMaterialService {

    private final MaterialMasterRepository materialMasterRepository;

    public List<MaterialSelectDto> getAllMaterials() {
        return materialMasterRepository.findAll().stream()
                .map(m -> MaterialSelectDto.builder()
                        .ingredientId(m.getIngredientId())
                        .ingredientName(m.getIngredientName())
                        .baseUnit(m.getBaseUnit())
                        .build()
                )
                .toList();
    }
}
