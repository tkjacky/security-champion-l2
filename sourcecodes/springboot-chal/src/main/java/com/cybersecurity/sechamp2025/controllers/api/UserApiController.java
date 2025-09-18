package com.cybersecurity.sechamp2025.controllers.api;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class UserApiController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid or expired token"));
        }

        // Return only the current authenticated user, not all users
        User currentUser = userService.findById(userId);
        if (currentUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        
        // Return as array for compatibility with frontend that expects data[0]
        return ResponseEntity.ok(Arrays.asList(currentUser));
    }
}
