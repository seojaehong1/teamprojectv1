package com.example.inventory.service;

import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;
import com.example.inventory.model.MaterialMaster;
import com.example.inventory.model.OptionMaster;
import com.example.inventory.model.Recipe;
import com.example.inventory.repository.MaterialMasterRepository;
import com.example.inventory.repository.OptionMasterRepository;
import com.example.inventory.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final MaterialMasterRepository materialMasterRepository;
    private final RecipeRepository recipeRepository;
    private final OptionMasterRepository optionMasterRepository;

    @Override
    @Transactional
    public InventoryResultDto processOrderStock(OrderStockRequestDto request) {

        if (request.getItems() == null || request.getItems().isEmpty()) { // 주문한 값을 받지 못했을 때
            return InventoryResultDto.builder()
                    .sucess(false)
                    .message("주문 항목이 비어 있습니다.")
                    .build();
        }

        // 메뉴 코드별 레시피&옵션 필요한 재료 총량 집계
        Map<String, BigDecimal> requiredMap = new HashMap<>(); // 재료명 -> 필요 수량

        for (OrderStockRequestDto.OrderItemDto item : request.getItems()) {
            String menuCode = item.getMenuCode();
            int qty = Optional.ofNullable(item.getQuantity()).orElse(1);

            // 레시피 기준 기본 재료
            List<Recipe> recipes = recipeRepository.findByMenuCode(menuCode);

            for (Recipe r : recipes) {
                String ingredientName = r.getMaterialMaster().getIngredientName();
                BigDecimal baseQty = r.getRequiredQuantity();
                BigDecimal totalForOrderItem = baseQty.multiply(BigDecimal.valueOf(qty));

                requiredMap.merge(ingredientName, totalForOrderItem, BigDecimal::add);
            }

            // 옵션에 따른 추가 재료
            List<Integer> optionIds = Optional.ofNullable(item.getOptionIds())
                    .orElse(Collections.emptyList());

            if (!optionIds.isEmpty()) {

                List<OptionMaster> options = optionMasterRepository.findByOptionIdIn(optionIds);

                for (OptionMaster opt : options) {

                    String fromMaterial = opt.getFromMaterial();
                    String toMaterial = opt.getToMaterial();
                    BigDecimal optQty = opt.getQuantity();       // 옵션 한 번 적용 시 필요한 양
                    String processMethod = opt.getProcessMethod();

                    if (optQty == null) continue;

                    BigDecimal totalOptQty = optQty.multiply(BigDecimal.valueOf(qty));

                    switch (processMethod) {

                        case "추가":
                            // to_material만 증가
                            if (toMaterial != null) {
                                requiredMap.merge(toMaterial, totalOptQty, BigDecimal::add);
                            }
                            break;

                        case "제거":
                            // from_material만 감소
                            if (fromMaterial != null) {
                                // 제거이므로 requiredQty를 음수로 추가
                                requiredMap.merge(fromMaterial, totalOptQty.negate(), BigDecimal::add);
                            }
                            break;

                        case "변경":
                            // 1) 기존 재료 from_material는 감소
                            if (fromMaterial != null) {
                                requiredMap.merge(fromMaterial, totalOptQty.negate(), BigDecimal::add);
                            }
                            // 2) 새 재료 to_material는 증가
                            if (toMaterial != null) {
                                requiredMap.merge(toMaterial, totalOptQty, BigDecimal::add);
                            }
                            break;
                    }
                }
            }
        }

        // 필요한가요?
        if (requiredMap.isEmpty()) {
            return InventoryResultDto.builder()
                    .sucess(true)
                    .message("차감할 재고가 없습니다.")
                    .build();
        }

        // 필요한 재료 목록에 대한 현재 재고 조회
        List<String> ingredientNames = new ArrayList<>(requiredMap.keySet());
        List<MaterialMaster> materials = materialMasterRepository.findByIngredientNameIn(ingredientNames);

        Map<String, MaterialMaster> materialMap = materials.stream()
                .collect(Collectors.toMap(MaterialMaster::getIngredientName, m -> m));

        List<String> insufficient = new ArrayList<>();

        // 재고 충분 여부
        for (String ingName : ingredientNames) {
            BigDecimal requireQty = requiredMap.get(ingName);
            MaterialMaster material = materialMap.get(ingName);

            if (material == null || material.getStockQty() == null) {
                insufficient.add(ingName + "(재고 정보 없음)");
                continue;
            }

            if (material.getStockQty().compareTo(requireQty) < 0) {
                insufficient.add(ingName + "(필요: " + requireQty + ", 보유:" + material.getStockQty() + ")");
            }
        }

        if (!insufficient.isEmpty()) {
            return InventoryResultDto.builder()
                    .sucess(false)
                    .message("재고가 부족한 재료가 있습니다.")
                    .insufficientItems(insufficient)
                    .build();
        }

        // 재료 차감 실행
        for (String ingName : ingredientNames) {
            BigDecimal requireQty = requiredMap.get(ingName);
            MaterialMaster material = materialMap.get(ingName);
            BigDecimal newQty = material.getStockQty().subtract(requireQty);
            material.setStockQty(newQty);
        }

        // 일괄 저장
        materialMasterRepository.saveAll(materials);

        return InventoryResultDto.builder()
                .sucess(true)
                .message("재고 차감 완료")
                .build();
    }
}
