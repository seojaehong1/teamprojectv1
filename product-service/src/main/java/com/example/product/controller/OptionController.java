package com.example.product.controller;

import com.example.product.dto.user.OptionGroupDto;
import com.example.product.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/menu/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @GetMapping
    public List<OptionGroupDto> getMenuOptions(
            @RequestParam Long menuCode
    ) {
        return optionService.getOptionsByMenu(menuCode);
    }
}
