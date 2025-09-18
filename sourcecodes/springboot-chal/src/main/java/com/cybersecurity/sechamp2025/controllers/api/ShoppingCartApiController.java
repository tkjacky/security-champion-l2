package com.cybersecurity.sechamp2025.controllers.api;

import com.cybersecurity.sechamp2025.models.User;
import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.models.CartItem;
import com.cybersecurity.sechamp2025.models.ShoppingCart;
import com.cybersecurity.sechamp2025.services.UserService;
import com.cybersecurity.sechamp2025.services.BookService;
import com.cybersecurity.sechamp2025.services.ShoppingCartService;
import com.cybersecurity.sechamp2025.services.UserBookService;
import com.cybersecurity.sechamp2025.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserBookService userBookService;

    private String getUserIdFromToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateAndExtractUserId(token);
    }

    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        List<CartItem> cartItems = shoppingCartService.getCartItems(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Book book = bookService.findById(item.getBookId());
            if (book != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId()); // Include cart item ID for updates/removal
                itemData.put("bookId", book.getId());
                itemData.put("title", book.getTitle());
                itemData.put("author", book.getAuthor());
                itemData.put("price", item.getPriceAtAdd());
                itemData.put("quantity", item.getQuantity());
                itemData.put("reservedUntil", item.getReservedUntil().toEpochMilli());
                itemData.put("imageUrl", book.getImageUrl());
                
                // Create book object for easier access in frontend
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", book.getId());
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("price", item.getPriceAtAdd());
                itemData.put("book", bookData);
                
                items.add(itemData);
            }
        }
        
        BigDecimal total = shoppingCartService.calculateCartTotal(userId);
        
        return ResponseEntity.ok(Map.of(
            "items", items,
            "totalPrice", total,
            "itemCount", items.size()
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Check user account status
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Cart access denied", 
                "message", "Your account status does not allow adding items to cart"
            ));
        }

        Object bookIdObj = request.get("bookId");
        if (bookIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book ID is required"));
        }
        String bookId = String.valueOf(bookIdObj);

        // Get quantity (optional, defaults to 1)
        Object quantityObj = request.get("quantity");
        Integer quantity = 1;
        if (quantityObj != null) {
            try {
                quantity = Integer.parseInt(String.valueOf(quantityObj));
                if (quantity <= 0) {
                    quantity = 1;
                }
            } catch (NumberFormatException e) {
                quantity = 1;
            }
        }

        try {
            CartItem item = shoppingCartService.addBookToCart(userId, bookId, quantity);
            Book book = bookService.findById(bookId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Book added to cart successfully",
                "bookTitle", book.getTitle(),
                "bookAuthor", book.getAuthor(),
                "price", item.getPriceAtAdd(),
                "quantity", item.getQuantity(),
                "totalPrice", item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())),
                "reservedUntil", item.getReservedUntil().toEpochMilli()
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> removeFromCart(@PathVariable String bookId, HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            shoppingCartService.removeBookFromCart(userId, bookId);
            return ResponseEntity.ok(Map.of("message", "Book removed from cart successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        List<CartItem> cartItems = shoppingCartService.getCartItems(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Book book = bookService.findById(item.getBookId());
            if (book != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId()); // Include cart item ID for updates/removal
                itemData.put("bookId", book.getId());
                itemData.put("title", book.getTitle());
                itemData.put("author", book.getAuthor());
                itemData.put("price", item.getPriceAtAdd());
                itemData.put("quantity", item.getQuantity());
                itemData.put("reservedUntil", item.getReservedUntil().toEpochMilli());
                itemData.put("imageUrl", book.getImageUrl());
                
                // Create book object for easier access in frontend
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", book.getId());
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("price", item.getPriceAtAdd());
                itemData.put("book", bookData);
                
                items.add(itemData);
            }
        }
        
        BigDecimal total = shoppingCartService.calculateCartTotal(userId);
        
        return ResponseEntity.ok(Map.of(
            "items", items,
            "totalPrice", total,
            "itemCount", items.size()
        ));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> startCheckout(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        try {
            // Get cart items before checkout
            List<CartItem> cartItems = shoppingCartService.getCartItems(userId);
            if (cartItems.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Cart is empty"));
            }

            // Calculate total
            BigDecimal total = shoppingCartService.calculateCartTotal(userId);
            
            // Check credit limit
            if (user.getCreditLimit().compareTo(total) < 0) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient credit limit",
                    "message", "Your credit limit ($" + user.getCreditLimit() + ") is insufficient for this purchase ($" + total + ")"
                ));
            }

            // Start checkout process
            shoppingCartService.startCheckout(userId);
            
            // Prepare checkout summary
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem item : cartItems) {
                Book book = bookService.findById(item.getBookId());
                if (book != null) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("bookId", book.getId());
                    itemData.put("title", book.getTitle());
                    itemData.put("author", book.getAuthor());
                    itemData.put("price", item.getPriceAtAdd());
                    items.add(itemData);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Checkout started successfully",
                "items", items,
                "totalPrice", total,
                "remainingCredit", user.getCreditLimit().subtract(total),
                "checkoutExpiresIn", 300 // 5 minutes in seconds
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/checkout/confirm")
    public ResponseEntity<?> confirmCheckout(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        try {
            // Get cart items for processing from CHECKING_OUT cart
            Optional<ShoppingCart> checkoutCartOpt = shoppingCartService.getCheckoutCart(userId);
            if (checkoutCartOpt.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "No checkout session found"));
            }
            
            ShoppingCart checkoutCart = checkoutCartOpt.get();
            List<CartItem> cartItems = shoppingCartService.getCartItemsFromCart(checkoutCart.getId());
            
            if (cartItems.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "No items in checkout"));
            }

            // RACE CONDITION VULNERABILITY: Multiple checks without proper locking
            // This allows for overselling when multiple users checkout simultaneously
            
            List<String> outOfStockBooks = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            
            // Check each book's availability (non-atomic operation)
            for (CartItem item : cartItems) {
                Book book = bookService.findById(item.getBookId());
                if (book == null) {
                    return ResponseEntity.status(400).body(Map.of("error", "Book not found: " + item.getBookId()));
                }
                
                // VULNERABILITY: This check is not atomic with the stock update below
                if (book.getStock() <= 0) {
                    outOfStockBooks.add(book.getTitle());
                }
                
                totalPrice = totalPrice.add(item.getPriceAtAdd());
            }
            
            if (!outOfStockBooks.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Some books are out of stock",
                    "outOfStockBooks", outOfStockBooks
                ));
            }
            
            // Check credit limit again
            if (user.getCreditLimit().compareTo(totalPrice) < 0) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient credit limit",
                    "message", "Credit limit check failed during checkout"
                ));
            }

            // RACE CONDITION: Process each book individually without transaction isolation
            // This allows multiple users to purchase the same last item
            
            List<Map<String, Object>> purchasedBooks = new ArrayList<>();
            
            for (CartItem item : cartItems) {
                Book book = bookService.findById(item.getBookId());
                
                // CRITICAL VULNERABILITY: Stock update is not atomic
                // Between this check and the update, another request could purchase the same book
                if (book.getStock() > 0) {
                    // Simulate some processing time that makes race condition more likely
                    try {
                        Thread.sleep(100); // 100ms delay makes race condition easier to exploit
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Update stock (non-atomic with the check above)
                    book.setStock(book.getStock() - 1);
                    bookService.save(book);
                    
                    // Add to user's library (allow multiple copies)
                    userBookService.addBookCopyToUser(userId, item.getBookId(), item.getPriceAtAdd());
                    
                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("title", book.getTitle());
                    bookData.put("author", book.getAuthor());
                    bookData.put("price", item.getPriceAtAdd());
                    purchasedBooks.add(bookData);
                } else {
                    // Book became out of stock during processing
                    shoppingCartService.cancelCheckout(userId);
                    return ResponseEntity.status(400).body(Map.of(
                        "error", "Stock changed during checkout",
                        "message", "The book '" + book.getTitle() + "' became out of stock during checkout"
                    ));
                }
            }
            
            // Update user's credit limit
            BigDecimal newCreditLimit = user.getCreditLimit().subtract(totalPrice);
            user.setCreditLimit(newCreditLimit);
            userService.updateUser(user);
            
            // Complete the checkout
            shoppingCartService.completeCheckout(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Checkout completed successfully",
                "purchasedBooks", purchasedBooks,
                "totalPrice", totalPrice,
                "remainingCredit", newCreditLimit
            ));
            
        } catch (Exception e) {
            // Log the detailed error for debugging
            System.err.println("Checkout confirmation error for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Cancel checkout on any error
            try {
                shoppingCartService.cancelCheckout(userId);
            } catch (Exception cancelError) {
                System.err.println("Error cancelling checkout: " + cancelError.getMessage());
            }
            
            return ResponseEntity.status(500).body(Map.of(
                "error", "Checkout failed",
                "message", "An error occurred during checkout: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/checkout/cancel")
    public ResponseEntity<?> cancelCheckout(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        shoppingCartService.cancelCheckout(userId);
        return ResponseEntity.ok(Map.of("message", "Checkout cancelled successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        shoppingCartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItemQuantity(@PathVariable Long itemId, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            Integer quantity = (Integer) request.get("quantity");
            if (quantity == null || quantity < 1) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid quantity"));
            }

            shoppingCartService.updateItemQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(Map.of("message", "Quantity updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long itemId, HttpServletRequest httpRequest) {
        String userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            shoppingCartService.removeCartItem(userId, itemId);
            return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
