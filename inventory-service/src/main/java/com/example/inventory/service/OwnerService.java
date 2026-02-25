package com.example.inventory.service;

import com.example.inventory.dto.InventoryDto;
import com.example.inventory.model.MaterialMaster;
import com.example.inventory.repository.MaterialMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final MaterialMasterRepository materialMasterRepository;

    @Transactional
    public InventoryDto.Response createInventory(InventoryDto.CreateRequest request) {
        // 1. DTO -> Entity 변환
        MaterialMaster material = MaterialMaster.builder()
                .ingredientName(request.getIngredientName())
                .stockQty(request.getStockQty())
                .baseUnit(request.getBaseUnit())
                .build();

        // 2. DB 저장
        MaterialMaster savedMaterial = materialMasterRepository.save(material);

        // 3. Entity -> Response DTO 반환
        return InventoryDto.Response.builder()
                .ingredientId(savedMaterial.getIngredientId())
                .ingredientName(savedMaterial.getIngredientName())
                .baseUnit(savedMaterial.getBaseUnit())
                .stockQty(savedMaterial.getStockQty())
                .build();
    }

    @Transactional(readOnly = true)
    public List<InventoryDto.Response> getAllInventory() {
        return materialMasterRepository.findAll().stream()
                .map(m -> InventoryDto.Response.builder()
                        .ingredientId(m.getIngredientId())
                        .ingredientName(m.getIngredientName())
                        .baseUnit(m.getBaseUnit())
                        .stockQty(m.getStockQty())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDto.UpdateResponse updateStock(Integer id, InventoryDto.UpdateRequest request) {
        MaterialMaster material = materialMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));

        material.setStockQty(request.getStockQty());

        return InventoryDto.UpdateResponse.builder()
                .message("재고가 수정되었습니다.")
                .ingredientId(material.getIngredientId())
                .stockQty(material.getStockQty())
                .build();
    }
}