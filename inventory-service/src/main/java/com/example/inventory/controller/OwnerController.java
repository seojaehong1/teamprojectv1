package com.example.inventory.controller;

import com.example.inventory.dto.InventoryDto;
import com.example.inventory.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/inventory")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public ResponseEntity<List<InventoryDto.Response>> getInventoryList() {
        return ResponseEntity.ok(ownerService.getAllInventory());
    }

    // [POST] 신규 재료 등록
    @PostMapping
    public ResponseEntity<InventoryDto.Response> createInventory(@RequestBody InventoryDto.CreateRequest request) {
        return ResponseEntity.ok(ownerService.createInventory(request));
    }

    @PutMapping("/{ingredientId}")
    public ResponseEntity<InventoryDto.UpdateResponse> updateStock(
            @PathVariable Integer ingredientId,
            @RequestBody InventoryDto.UpdateRequest request) {
        return ResponseEntity.ok(ownerService.updateStock(ingredientId, request));
    }
}