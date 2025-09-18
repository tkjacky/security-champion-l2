package com.cybersecurity.sechamp2025.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 100)
    private String author;
    
    @Column(unique = true, length = 20)
    private String isbn;
    
    @Column(length = 50)
    private String category;
    
    private java.math.BigDecimal price;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Integer stock;
    
    private java.math.BigDecimal rating;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(length = 100)
    private String publisher;
    
    @Column(name = "publish_date", length = 20)
    private String publishDate;
    
    // JPA relationship with reviews
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<BookReview> reviews;
    
    public Book() {
    }
    
    public Book(String id, String title, String author, String isbn, String category, 
                Double price, String description, Integer stock, Double rating, 
                String imageUrl, String publisher, String publishDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.price = price != null ? java.math.BigDecimal.valueOf(price) : null;
        this.description = description;
        this.stock = stock;
        this.rating = rating != null ? java.math.BigDecimal.valueOf(rating) : null;
        this.imageUrl = imageUrl;
        this.publisher = publisher;
        this.publishDate = publishDate;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    // Helper method for backward compatibility
    public Double getPriceAsDouble() {
        return price != null ? price.doubleValue() : null;
    }
    
    public void setPriceFromDouble(Double price) {
        this.price = price != null ? java.math.BigDecimal.valueOf(price) : null;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public java.math.BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(java.math.BigDecimal rating) {
        this.rating = rating;
    }
    
    // Helper method for backward compatibility
    public Double getRatingAsDouble() {
        return rating != null ? rating.doubleValue() : null;
    }
    
    public void setRatingFromDouble(Double rating) {
        this.rating = rating != null ? java.math.BigDecimal.valueOf(rating) : null;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
    
    public java.util.List<BookReview> getReviews() {
        return reviews;
    }
    
    public void setReviews(java.util.List<BookReview> reviews) {
        this.reviews = reviews;
    }
}
