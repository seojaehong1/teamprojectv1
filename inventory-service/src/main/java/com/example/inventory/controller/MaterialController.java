package com.example.inventory.controller;

import com.example.inventory.model.MaterialMaster;
import com.example.inventory.repository.MaterialMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialMasterRepository materialMasterRepository;

    @GetMapping
    public String listMaterials(Model model) {
        List<MaterialMaster> materials = materialMasterRepository.findAll();
        model.addAttribute("materials", materials);
        return "materials"; // templates/materials.html
    }
}
