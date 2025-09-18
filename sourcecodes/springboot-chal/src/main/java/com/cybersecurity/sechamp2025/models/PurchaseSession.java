package com.cybersecurity.sechamp2025.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "purchase_sessions")
public class PurchaseSession {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "book_id", nullable = true, length = 50) // Now nullable for cart-based sessions
    private String bookId;
    
    @Column(name = "cart_id", nullable = true, length = 50) // Reference to shopping cart
    private String cartId;
    
    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @Column(length = 20)
    private String status;
    
    // Constructors
    public PurchaseSession() {
    }
    
    // Constructor for single book purchase (legacy support)
    public PurchaseSession(String id, String userId, String bookId) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.cartId = null;
        // Use Instant for proper UTC timezone handling
        Instant now = Instant.now();
        this.lockedAt = now;
        this.expiresAt = now.plusSeconds(15 * 60); // 15 minutes in seconds
        this.status = "LOCKED";
    }
    
    // Constructor for cart-based purchase
    public PurchaseSession(String id, String userId, String cartId, boolean isCartSession) {
        this.id = id;
        this.userId = userId;
        this.bookId = null;
        this.cartId = cartId;
        // Use Instant for proper UTC timezone handling
        Instant now = Instant.now();
        this.lockedAt = now;
        this.expiresAt = now.plusSeconds(15 * 60); // 15 minutes in seconds
        this.status = "LOCKED";
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getCartId() {
        return cartId;
    }
    
    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
    
    public Instant getLockedAt() {
        return lockedAt;
    }
    
    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isExpired() {
        // Use Instant for proper UTC timezone handling
        Instant now = Instant.now();
        boolean expired = now.isAfter(expiresAt);
        
        // Debug logging
        System.out.println("Session expiration check:");
        System.out.println("  Current UTC time: " + now);
        System.out.println("  Expires at: " + expiresAt);
        System.out.println("  Is expired: " + expired);
        
        return expired;
    }
}
