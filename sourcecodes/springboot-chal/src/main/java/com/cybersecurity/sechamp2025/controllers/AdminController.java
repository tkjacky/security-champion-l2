package com.cybersecurity.sechamp2025.controllers;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.services.BookService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;
import com.cybersecurity.sechamp2025.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/admin")
    public String admin(Model model) {
        return "admin";
    }

    // Get all users for admin panel
    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Update user (admin function)
    @PostMapping("/api/admin/users/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable String targetUserId,
                                      @RequestBody Map<String, Object> updateData,
                                      HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String adminUserId = JwtUtil.validateAndExtractUserId(token);
        if (adminUserId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User admin = userService.findById(adminUserId);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        User targetUser = userService.findById(targetUserId);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Update user fields
        if (updateData.containsKey("name")) {
            targetUser.setName((String) updateData.get("name"));
        }
        if (updateData.containsKey("phone")) {
            targetUser.setPhone((String) updateData.get("phone"));
        }
        if (updateData.containsKey("address")) {
            targetUser.setAddress((String) updateData.get("address"));
        }
        if (updateData.containsKey("role")) {
            targetUser.setRole((String) updateData.get("role"));
        }
        if (updateData.containsKey("accountStatus")) {
            targetUser.setAccountStatus((String) updateData.get("accountStatus"));
        }
        if (updateData.containsKey("isAdmin")) {
            targetUser.setAdmin((Boolean) updateData.get("isAdmin"));
        }
        if (updateData.containsKey("creditLimit")) {
            Object creditLimitObj = updateData.get("creditLimit");
            if (creditLimitObj instanceof Number) {
                double creditLimitValue = ((Number) creditLimitObj).doubleValue();
                
                // Validate credit limit range (0 to 10,000)
                if (creditLimitValue < 0 || creditLimitValue > 10000) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid credit limit", 
                        "message", "Credit limit must be between 0 and 10,000"
                    ));
                }
                
                targetUser.setCreditLimit(BigDecimal.valueOf(creditLimitValue));
            }
        }
        if (updateData.containsKey("newsletter")) {
            targetUser.setNewsletter((Boolean) updateData.get("newsletter"));
        }
        if (updateData.containsKey("promotions")) {
            targetUser.setPromotions((Boolean) updateData.get("promotions"));
        }

        userService.updateUser(targetUser);

        System.out.println("[ADMIN] User updated by admin:");
        System.out.println("  Admin User ID: " + adminUserId);
        System.out.println("  Target User ID: " + targetUserId);
        System.out.println("  Updated fields: " + updateData.keySet());

        return ResponseEntity.ok(Map.of(
            "message", "User updated successfully",
            "user", targetUser
        ));
    }

    // Reset user password (admin function)
    @PostMapping("/api/admin/users/{targetUserId}/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetUserPassword(@PathVariable String targetUserId,
                                             @RequestBody Map<String, Object> passwordData,
                                             HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String adminUserId = JwtUtil.validateAndExtractUserId(token);
        if (adminUserId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User admin = userService.findById(adminUserId);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        User targetUser = userService.findById(targetUserId);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        String newPassword = (String) passwordData.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
        }

        // Update password
        targetUser.setPassword(PasswordUtil.hash(newPassword));
        userService.updateUser(targetUser);

        System.out.println("[ADMIN] Password reset by admin:");
        System.out.println("  Admin User ID: " + adminUserId);
        System.out.println("  Target User ID: " + targetUserId);
        System.out.println("  Target User Email: " + targetUser.getEmail());

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // Delete user (admin function)
    @DeleteMapping("/api/admin/users/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable String targetUserId,
                                      HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String adminUserId = JwtUtil.validateAndExtractUserId(token);
        if (adminUserId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User admin = userService.findById(adminUserId);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        User targetUser = userService.findById(targetUserId);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Prevent admin from deleting themselves
        if (adminUserId.equals(targetUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete your own account"));
        }

        userService.deleteUser(targetUserId);

        System.out.println("[ADMIN] User deleted by admin:");
        System.out.println("  Admin User ID: " + adminUserId);
        System.out.println("  Deleted User ID: " + targetUserId);
        System.out.println("  Deleted User Email: " + targetUser.getEmail());

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // Book Management Endpoints

    // Get all books for admin panel
    @GetMapping("/api/admin/books")
    @ResponseBody
    public ResponseEntity<?> getAllBooks(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        List<Book> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    // Get a specific book by ID
    @GetMapping("/api/admin/books/{bookId}")
    @ResponseBody
    public ResponseEntity<?> getBook(@PathVariable String bookId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
        }

        return ResponseEntity.ok(book);
    }

    // Add new book
    @PostMapping("/api/admin/books")
    @ResponseBody
    public ResponseEntity<?> addBook(@RequestBody Map<String, Object> bookData, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        // Create new book
        Book book = new Book();
        book.setId(UUID.randomUUID().toString().substring(0, 8)); // Generate 8-character ID
        updateBookFromData(book, bookData);

        try {
            Book savedBook = bookService.save(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save book: " + e.getMessage()));
        }
    }

    // Update existing book
    @PutMapping("/api/admin/books/{bookId}")
    @ResponseBody
    public ResponseEntity<?> updateBook(@PathVariable String bookId,
                                       @RequestBody Map<String, Object> bookData,
                                       HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
        }

        updateBookFromData(book, bookData);

        try {
            Book savedBook = bookService.save(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update book: " + e.getMessage()));
        }
    }

    // Delete book
    @DeleteMapping("/api/admin/books/{bookId}")
    @ResponseBody
    public ResponseEntity<?> deleteBook(@PathVariable String bookId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing authorization"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid token"));
        }

        User currentUser = userService.findById(userId);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
        }

        try {
            bookService.deleteById(bookId);
            return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete book: " + e.getMessage()));
        }
    }

    // Helper method to update book object from request data
    private void updateBookFromData(Book book, Map<String, Object> bookData) {
        if (bookData.containsKey("title")) {
            book.setTitle((String) bookData.get("title"));
        }
        if (bookData.containsKey("author")) {
            book.setAuthor((String) bookData.get("author"));
        }
        if (bookData.containsKey("isbn")) {
            book.setIsbn((String) bookData.get("isbn"));
        }
        if (bookData.containsKey("category")) {
            book.setCategory((String) bookData.get("category"));
        }
        if (bookData.containsKey("price")) {
            Object priceObj = bookData.get("price");
            if (priceObj instanceof Number) {
                book.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }
        }
        if (bookData.containsKey("stock")) {
            Object stockObj = bookData.get("stock");
            if (stockObj instanceof Number) {
                book.setStock(((Number) stockObj).intValue());
            }
        }
        if (bookData.containsKey("rating")) {
            Object ratingObj = bookData.get("rating");
            if (ratingObj instanceof Number) {
                book.setRating(BigDecimal.valueOf(((Number) ratingObj).doubleValue()));
            } else if (ratingObj == null) {
                book.setRating(null);
            }
        }
        if (bookData.containsKey("publisher")) {
            book.setPublisher((String) bookData.get("publisher"));
        }
        if (bookData.containsKey("publishDate")) {
            book.setPublishDate((String) bookData.get("publishDate"));
        }
        if (bookData.containsKey("description")) {
            book.setDescription((String) bookData.get("description"));
        }
        if (bookData.containsKey("imageUrl")) {
            book.setImageUrl((String) bookData.get("imageUrl"));
        }
    }
}
