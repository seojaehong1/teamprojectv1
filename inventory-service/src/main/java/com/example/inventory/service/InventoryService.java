package com.example.inventory.service;

import com.example.inventory.dto.InventoryResultDto;
import com.example.inventory.dto.OrderStockRequestDto;

public interface InventoryService {
    InventoryResultDto processOrderStock(OrderStockRequestDto request);
}
