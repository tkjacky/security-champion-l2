package com.cybersecurity.sechamp2025.controllers.api;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;
import com.cybersecurity.sechamp2025.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        User user = userService.findByEmail(email);
        if (user != null && PasswordUtil.matches(password, user.getPassword())) {
            String token = JwtUtil.generateToken(user.getId());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "message", "Login successful"
            ));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Basic validation
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        // Check if email already exists
        if (userService.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

       
        // Generate ID and hash password
        user.setId(UUID.randomUUID().toString());
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        
        // Set defaults
        if (user.getAccountStatus() == null) {
            user.setAccountStatus("PENDING");
        }        
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getCreditLimit() == null) {
            user.setCreditLimit(BigDecimal.valueOf(20.0));
        }
        
        if ("ADMIN".equals(user.getRole())) {
            user.setAdmin(true);
        }
        
        // Add user to service
        userService.addUser(user);
        
        // Log the registration for security monitoring 
        System.out.println("[SECURITY] New user registered:");
        System.out.println("  ID: " + user.getId());
        System.out.println("  Name: " + user.getName());
        System.out.println("  Email: " + user.getEmail());
        System.out.println("  Role: " + user.getRole());
        System.out.println("  IsAdmin: " + user.isAdmin());
        System.out.println("  AccountStatus: " + user.getAccountStatus());
        System.out.println("  CreditLimit: " + user.getCreditLimit());

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "userId", user.getId()
        ));
    }

    public static String getUserIdByToken(String token) {
        return JwtUtil.validateAndExtractUserId(token);
    }
}
