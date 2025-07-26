package com.example.zzk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoPageController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/demo";
    }
    
    @GetMapping("/demo")
    public String demo() {
        return "performance_demo";
    }
}
