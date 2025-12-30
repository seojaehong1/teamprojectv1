package com.example.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResultDto { // 재고 처리 결과 반환 DTO

    private boolean sucess;
    private String message;
    private List<String> insufficientItems;
}
