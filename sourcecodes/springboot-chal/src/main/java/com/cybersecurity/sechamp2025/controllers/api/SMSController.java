package com.cybersecurity.sechamp2025.controllers.api;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class SMSController {

    @Autowired
    private UserService userService;

    @GetMapping("/self-service-sms")
    public ResponseEntity<Map<String, Object>> handleSmsRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String userId) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", 401);
            response.put("error", "Unauthorized: Missing or invalid token");
            return ResponseEntity.ok(response);
        }

        if (userId == null || userId.trim().isEmpty()) {
            response.put("status", 400);
            response.put("error", "Missing required parameter: userId");
            return ResponseEntity.ok(response);
        }

        // Lookup user
        User user = userService.findById(userId);
        if (user == null) {
            response.put("status", 404);
            response.put("error", "User not found");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("address", user.getAddress());
        
        userInfo.put("role", user.getRole());
        userInfo.put("isAdmin", user.isAdmin());
        userInfo.put("accountStatus", user.getAccountStatus());
        userInfo.put("creditLimit", user.getCreditLimit());

        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
        String smsMessage = "Pretending to send SMS to " + user.getPhone() + " with verification code " + verificationCode;
        
        System.out.println("[SMSController] " + smsMessage);
        System.out.println("[SECURITY] User data exposed via SMS endpoint: " + user.getEmail());

        response.put("status", 200);
        response.put("data", userInfo);
        response.put("message", smsMessage);
        response.put("verificationCode", verificationCode);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sms/send")
    public ResponseEntity<Map<String, Object>> sendSms(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        Object userIdObj = request.get("userId");
        if (userIdObj == null) {
            response.put("error", "Missing userId parameter");
            return ResponseEntity.badRequest().body(response);
        }
        
        String userId = userIdObj.toString();
        User user = userService.findById(userId);
        
        if (user == null) {
            response.put("error", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
        String message = "SecBooks verification code: " + verificationCode + ". Valid for 10 minutes.";
        
        System.out.println("[SMS] Sending to " + user.getPhone() + ": " + message);
        
        response.put("message", "SMS sent successfully to " + user.getPhone());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
}
