package com.cybersecurity.sechamp2025.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @Column(length = 20)
    private String status; // ACTIVE, CHECKING_OUT, COMPLETED, EXPIRED
    
    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();
    
    // Constructors
    public ShoppingCart() {
    }
    
    public ShoppingCart(String id, String userId) {
        this.id = id;
        this.userId = userId;
        Instant now = Instant.now();
        this.createdAt = now;
        this.expiresAt = now.plusSeconds(30 * 60); // 30 minutes session
        this.status = "ACTIVE";
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
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public boolean isExpired() {
        Instant now = Instant.now();
        return now.isAfter(expiresAt);
    }
    
    public void extendExpiration() {
        // Extend cart expiration by 30 minutes when items are added
        this.expiresAt = Instant.now().plusSeconds(30 * 60);
    }
}
