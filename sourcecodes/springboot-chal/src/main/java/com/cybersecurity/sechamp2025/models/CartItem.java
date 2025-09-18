package com.cybersecurity.sechamp2025.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    private ShoppingCart shoppingCart;
    
    @Column(name = "book_id", nullable = false, length = 50)
    private String bookId;
    
    @Column(nullable = false)
    private Integer quantity = 1; // Allow multiple quantities
    
    @Column(name = "price_at_add")
    private BigDecimal priceAtAdd; // Store price when added to cart
    
    @Column(name = "added_at")
    private Instant addedAt;
    
    @Column(name = "reserved_until")
    private Instant reservedUntil; // Temporary stock reservation
    
    // Constructors
    public CartItem() {
    }
    
    public CartItem(String id, ShoppingCart shoppingCart, String bookId, BigDecimal priceAtAdd, Integer quantity) {
        this.id = id;
        this.shoppingCart = shoppingCart;
        this.bookId = bookId;
        this.quantity = quantity != null && quantity > 0 ? quantity : 1;
        this.priceAtAdd = priceAtAdd;
        this.addedAt = Instant.now();
        this.reservedUntil = Instant.now().plusSeconds(15 * 60); // 15 minute reservation
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }
    
    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPriceAtAdd() {
        return priceAtAdd;
    }
    
    public void setPriceAtAdd(BigDecimal priceAtAdd) {
        this.priceAtAdd = priceAtAdd;
    }
    
    public Instant getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
    
    public Instant getReservedUntil() {
        return reservedUntil;
    }
    
    public void setReservedUntil(Instant reservedUntil) {
        this.reservedUntil = reservedUntil;
    }
    
    public boolean isReservationExpired() {
        return Instant.now().isAfter(reservedUntil);
    }
    
    public void extendReservation() {
        this.reservedUntil = Instant.now().plusSeconds(15 * 60);
    }
}
