package com.cybersecurity.sechamp2025.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home"; // This will resolve to templates/home.html
    }
    
    @GetMapping("/api-test")
    public String apiTest() {
        return "api_test"; // This will resolve to templates/api_test.html
    }
}
