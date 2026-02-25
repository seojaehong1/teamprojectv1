package com.example.boardservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bbs")
public class EventController {
    
    @GetMapping("/event")
    public String eventPage() {
        return "bbs/event";
    }
}
