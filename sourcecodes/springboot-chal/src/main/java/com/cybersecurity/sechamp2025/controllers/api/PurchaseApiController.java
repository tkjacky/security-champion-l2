package com.cybersecurity.sechamp2025.controllers.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.models.PurchaseSession;
import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.services.BookService;
import com.cybersecurity.sechamp2025.services.PurchaseSessionService;
import com.cybersecurity.sechamp2025.services.UserBookService;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PurchaseSessionService purchaseSessionService;

    @Autowired
    private UserBookService userBookService;

    @PostMapping("/book")
    public ResponseEntity<?> initiatePurchase(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        // Get user from token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Check if user status is PENDING
        if ("PENDING".equals(user.getAccountStatus())) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Purchase not allowed", 
                "message", "Your account is pending approval. Please contact an administrator to activate your account before making purchases."
            ));
        }

        // Check if user account is not ACTIVE
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Purchase not allowed", 
                "message", "Your account status does not allow purchases. Current status: " + user.getAccountStatus()
            ));
        }

        // Get book details
        Object bookIdObj = request.get("bookId");
        if (bookIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book ID is required"));
        }
        String bookId = String.valueOf(bookIdObj);

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
        }

        // Check if book is in stock (considering locked sessions)
        int availableStock = book.getStock() - purchaseSessionService.getLockedSessionsForBook(bookId).size();
        if (book.getStock() == null || book.getStock() <= 0) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Out of stock", 
                "message", "This book is currently out of stock"
            ));
        }

        if (availableStock <= 0) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Temporarily unavailable", 
                "message", "This book is temporarily locked by other users. Please try again in a few minutes."
            ));
        }

        // Check if user has sufficient credit limit
        BigDecimal bookPrice = book.getPrice();
        if (bookPrice != null && user.getCreditLimit().compareTo(bookPrice) < 0) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Insufficient credit limit", 
                "message", "Your credit limit ($" + user.getCreditLimit() + ") is insufficient for this purchase ($" + bookPrice + ")"
            ));
        }

        // Create purchase session to temporarily lock the book
        PurchaseSession session = purchaseSessionService.createSession(userId, bookId);

        // Return session info for confirmation page
        return ResponseEntity.ok(Map.of(
            "sessionId", session.getId(),
            "message", "Book temporarily reserved",
            "bookTitle", book.getTitle(),
            "bookAuthor", book.getAuthor(),
            "price", bookPrice,
            "expiresAt", session.getExpiresAt().toEpochMilli(),
            "remainingCredit", user.getCreditLimit().subtract(bookPrice != null ? bookPrice : BigDecimal.ZERO)
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPurchase(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        // Get user from token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Get session ID
        Object sessionIdObj = request.get("sessionId");
        if (sessionIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Session ID is required"));
        }
        String sessionId = String.valueOf(sessionIdObj);

        // Get purchase session
        Optional<PurchaseSession> sessionOpt = purchaseSessionService.getSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Purchase session not found or expired"));
        }

        PurchaseSession session = sessionOpt.get();
        
        // Verify session belongs to user
        if (!session.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Session does not belong to user"));
        }

        // Check if session is expired
        if (session.isExpired()) {
            purchaseSessionService.cancelSession(sessionId);
            return ResponseEntity.status(400).body(Map.of("error", "Purchase session has expired"));
        }

        // Get book details
        Book book = bookService.findById(session.getBookId());
        if (book == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
        }

        // REMOVED: Stock validation check that prevents race condition
        // The original check here prevented negative stock, which defeats the race condition test
        // if (book.getStock() == null || book.getStock() <= 0) { ... }
        
        // Check credit limit again
        BigDecimal bookPrice = book.getPrice();
        if (bookPrice != null && user.getCreditLimit().compareTo(bookPrice) < 0) {
            purchaseSessionService.cancelSession(sessionId);
            return ResponseEntity.status(400).body(Map.of(
                "error", "Insufficient credit limit", 
                "message", "Your credit limit is insufficient for this purchase"
            ));
        }

        try {
            // Complete the purchase with RACE CONDITION VULNERABILITY
            // Instead of proper atomic operation, use vulnerable non-atomic approach
            
            // VULNERABLE: Non-atomic stock decrement (read-then-modify-then-save)
            // Multiple threads can read the same stock value simultaneously
            Book currentBook = bookService.findById(session.getBookId());
            int currentStock = currentBook.getStock();
            
            // This gap allows race conditions - other threads can modify stock here
            // No synchronization or atomic operations protect this critical section
            currentBook.setStock(currentStock - 1);  // Allows negative stock
            bookService.save(currentBook);

            // Update user's credit limit
            BigDecimal newCreditLimit = user.getCreditLimit().subtract(bookPrice != null ? bookPrice : BigDecimal.ZERO);
            user.setCreditLimit(newCreditLimit);
            userService.updateUser(user);

            // Add book to user's library (allow multiple copies)
            userBookService.addBookCopyToUser(userId, session.getBookId(), bookPrice);

            // Complete the session
            purchaseSessionService.completeSession(sessionId);

            return ResponseEntity.ok(Map.of(
                "message", "Purchase completed successfully",
                "bookTitle", currentBook.getTitle(),
                "price", bookPrice,
                "remainingCredit", newCreditLimit,
                "remainingStock", currentBook.getStock()
            ));

        } catch (Exception e) {
            // If anything fails, cancel the session
            purchaseSessionService.cancelSession(sessionId);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Purchase failed", 
                "message", "An error occurred while processing your purchase: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPurchase(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        // Get user from token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        // Get session ID
        Object sessionIdObj = request.get("sessionId");
        if (sessionIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Session ID is required"));
        }
        String sessionId = String.valueOf(sessionIdObj);

        // Get purchase session
        Optional<PurchaseSession> sessionOpt = purchaseSessionService.getSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Session already expired or cancelled"));
        }

        PurchaseSession session = sessionOpt.get();
        
        // Verify session belongs to user
        if (!session.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Session does not belong to user"));
        }

        // Cancel the session
        purchaseSessionService.cancelSession(sessionId);

        return ResponseEntity.ok(Map.of("message", "Purchase cancelled successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getPurchaseStatus(HttpServletRequest httpRequest) {
        // Get user from token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        boolean canPurchase = "ACTIVE".equals(user.getAccountStatus());
        
        return ResponseEntity.ok(Map.of(
            "canPurchase", canPurchase,
            "accountStatus", user.getAccountStatus(),
            "creditLimit", user.getCreditLimit(),
            "message", canPurchase ? "Account is active and ready for purchases" : 
                      "PENDING".equals(user.getAccountStatus()) ? 
                      "Account is pending approval. Contact admin to activate." :
                      "Account status does not allow purchases: " + user.getAccountStatus()
        ));
    }

    // RACE CONDITION TEST ENDPOINT - Direct vulnerable purchase without session locking
    @PostMapping("/race-test/{bookId}")
    public ResponseEntity<?> raceConditionTest(@PathVariable String bookId, HttpServletRequest httpRequest) {
        // Get user from token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.validateAndExtractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        // Direct vulnerable stock decrement without any session protection
        try {
            // VULNERABLE: Non-atomic stock decrement (read-then-modify-then-save)
            // Multiple threads can read the same stock value simultaneously
            Book book = bookService.findById(bookId);
            if (book == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Book not found"));
            }
            
            // This is the vulnerable section - no synchronization or atomic operations
            int currentStock = book.getStock() != null ? book.getStock() : 0;
            
            // Gap here allows race conditions - other threads can read the same stock value
            // No session locking or atomic operations protect this critical section
            book.setStock(currentStock - 1);  // This allows negative stock!
            bookService.save(book);
            
            // Get the updated stock after save
            Book updatedBook = bookService.findById(bookId);
            int finalStock = updatedBook != null && updatedBook.getStock() != null ? updatedBook.getStock() : 0;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "bookId", bookId,
                "title", updatedBook != null ? updatedBook.getTitle() : "Unknown",
                "currentStock", finalStock,
                "message", "Direct race condition test - stock decremented without protection"
            ));
            
        } catch (Exception e) {
            Book book = bookService.findById(bookId);
            int stock = (book != null && book.getStock() != null) ? book.getStock() : 0;
            return ResponseEntity.ok(Map.of(
                "success", false,
                "bookId", bookId,
                "title", book != null ? book.getTitle() : "Unknown",
                "currentStock", stock,
                "message", "Race condition test error: " + e.getMessage()
            ));
        }
    }
}
