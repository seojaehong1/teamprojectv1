package com.example.inventory.controller;

import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;
import com.example.inventory.service.InventoryService;
import com.example.inventory.model.OptionMaster;
import com.example.inventory.repository.OptionMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final OptionMasterRepository optionMasterRepository;

    @PostMapping("/process")
    public ResponseEntity<InventoryResultDto> processOrder(@RequestBody OrderStockRequestDto request) {
        InventoryResultDto result = inventoryService.processOrderStock(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionMaster>> listOptions() {
        List<OptionMaster> options = optionMasterRepository.findAll();
        return ResponseEntity.ok(options);
    }
}
