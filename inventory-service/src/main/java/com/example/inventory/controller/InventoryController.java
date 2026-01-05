package com.example.inventory.controller;

import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/process")
    public ResponseEntity<InventoryResultDto> processOrder(@RequestBody OrderStockRequestDto request) {
        InventoryResultDto result = inventoryService.processOrderStock(request);
        return ResponseEntity.ok(result);
    }
}
