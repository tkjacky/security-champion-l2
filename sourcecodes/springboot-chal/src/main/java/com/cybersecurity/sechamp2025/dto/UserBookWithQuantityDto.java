package com.cybersecurity.sechamp2025.dto;

import com.cybersecurity.sechamp2025.models.Book;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserBookWithQuantityDto {
    private Book book;
    private int quantity;
    private LocalDateTime firstPurchaseDate;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal totalSpent;
    
    public UserBookWithQuantityDto() {
    }
    
    public UserBookWithQuantityDto(Book book, int quantity, LocalDateTime firstPurchaseDate, 
                                 LocalDateTime lastPurchaseDate, BigDecimal totalSpent) {
        this.book = book;
        this.quantity = quantity;
        this.firstPurchaseDate = firstPurchaseDate;
        this.lastPurchaseDate = lastPurchaseDate;
        this.totalSpent = totalSpent;
    }
    
    // Getters and Setters
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getFirstPurchaseDate() {
        return firstPurchaseDate;
    }
    
    public void setFirstPurchaseDate(LocalDateTime firstPurchaseDate) {
        this.firstPurchaseDate = firstPurchaseDate;
    }
    
    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
}
