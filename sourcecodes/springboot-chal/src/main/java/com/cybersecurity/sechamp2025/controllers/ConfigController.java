package com.cybersecurity.sechamp2025.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @GetMapping("/bak/config.json")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("version", "1.0.0");
        config.put("environment", "production");
        
        Map<String, String> api = new HashMap<>();
        api.put("publicSmsEndpoint", "/api/public/self-service-sms");
        api.put("authEndpoint", "/api/auth/login");
        api.put("userServiceEndpoint", "/api/user");
        api.put("registrationEndpoint", "/api/auth/register");
        config.put("api", api);
        
        Map<String, String> support = new HashMap<>();
        support.put("email", "support@secbooks.com");
        config.put("support", support);
        
        Map<String, Object> features = new HashMap<>();
        features.put("enableSms", true);
        features.put("enableLogging", true);
        features.put("allowPublicConfig", true);
        features.put("massAssignmentEnabled", true);
        config.put("features", features);
        
        Map<String, String> meta = new HashMap<>();
        meta.put("generatedAt", "2025-07-22T10:00:00Z");
        meta.put("author", "devops@secbooks.com");
        config.put("meta", meta);
        
        System.out.println("[SECURITY WARNING] Config file accessed from public endpoint");
        
        return ResponseEntity.ok(config);
    }
}
