package com.cybersecurity.sechamp2025.controllers;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.models.UserBook;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.services.UserBookService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;
import com.cybersecurity.sechamp2025.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBookService userBookService;

    @GetMapping("/profile")
    public String profile(Model model) {
        // For now, we'll let JavaScript handle authentication
        // The template will redirect to login if no token is found
        return "profile";
    }

    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(user);
    }

    // VULNERABILITY: API endpoint that doesn't properly validate user ownership
    @PostMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updateData,
                                         HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String authenticatedUserId = JwtUtil.validateAndExtractUserId(token);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        // VULNERABILITY: Uses userId from request body instead of authenticated user ID
        // This allows attackers to modify other users' data by tampering with userId
        String targetUserId = (String) updateData.get("userId");
        if (targetUserId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing userId"));
        }

        User user = userService.findById(targetUserId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Update user fields from request
        if (updateData.containsKey("name")) {
            user.setName((String) updateData.get("name"));
        }
        if (updateData.containsKey("phone")) {
            user.setPhone((String) updateData.get("phone"));
        }
        if (updateData.containsKey("address")) {
            user.setAddress((String) updateData.get("address"));
        }
        if (updateData.containsKey("newsletter")) {
            user.setNewsletter((Boolean) updateData.get("newsletter"));
        }
        if (updateData.containsKey("promotions")) {
            user.setPromotions((Boolean) updateData.get("promotions"));
        }

        userService.updateUser(user);

        System.out.println("[SECURITY] Profile updated:");
        System.out.println("  Authenticated User ID: " + authenticatedUserId);
        System.out.println("  Target User ID: " + targetUserId);
        System.out.println("  Same User: " + authenticatedUserId.equals(targetUserId));

        return ResponseEntity.ok(Map.of(
            "message", "Profile updated successfully",
            "user", user
        ));
    }

    // VULNERABILITY: Password change endpoint with user ID tampering
    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> passwordData,
                                          HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String authenticatedUserId = JwtUtil.validateAndExtractUserId(token);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        // VULNERABILITY: Uses userId from request body instead of authenticated user ID
        String targetUserId = (String) passwordData.get("userId");
        String currentPassword = (String) passwordData.get("currentPassword");
        String newPassword = (String) passwordData.get("newPassword");

        if (targetUserId == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        User user = userService.findById(targetUserId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Only verify current password if changing own password
        if (authenticatedUserId.equals(targetUserId)) {
            if (currentPassword == null || !PasswordUtil.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(400).body(Map.of("error", "Current password is incorrect"));
            }
        }
        // VULNERABILITY: If targetUserId != authenticatedUserId, we skip password verification!

        // Update password
        user.setPassword(PasswordUtil.hash(newPassword));
        userService.updateUser(user);

        System.out.println("[SECURITY] Password changed:");
        System.out.println("  Authenticated User ID: " + authenticatedUserId);
        System.out.println("  Target User ID: " + targetUserId);
        System.out.println("  Same User: " + authenticatedUserId.equals(targetUserId));
        System.out.println("  Password Verified: " + (currentPassword != null));

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/api/user/books")
    @ResponseBody
    public ResponseEntity<?> getUserBooks(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        List<UserBook> userBooks = userBookService.getUserBooks(userId);
        return ResponseEntity.ok(userBooks);
    }

    @GetMapping("/api/user/books-with-quantities")
    @ResponseBody
    public ResponseEntity<?> getUserBooksWithQuantities(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(userBookService.getUserBooksWithQuantities(userId));
    }
}
