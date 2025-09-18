package com.cybersecurity.sechamp2025.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoApiController {

    @GetMapping("/greet")
    public ResponseEntity<Map<String, String>> greet(@RequestParam(defaultValue = "Guest") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, String>> echo(@RequestBody Map<String, String> payload) {
        String input = payload.getOrDefault("input", "nothing");
        Map<String, String> response = new HashMap<>();
        response.put("echo", input);
        return ResponseEntity.ok(response);
    }
}
