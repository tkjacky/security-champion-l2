package com.cybersecurity.sechamp2025.repositories;

import com.cybersecurity.sechamp2025.models.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, String> {
    
    List<BookReview> findByBookIdOrderByCreatedAtDesc(String bookId);
    
    List<BookReview> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<BookReview> findByRatingGreaterThanEqualOrderByCreatedAtDesc(BigDecimal rating);
    
    List<BookReview> findByVerifiedPurchaseTrueOrderByHelpfulCountDesc();
    
    @Query("SELECT AVG(br.rating) FROM BookReview br WHERE br.bookId = :bookId")
    BigDecimal getAverageRatingForBook(@Param("bookId") String bookId);
    
    @Query("SELECT COUNT(br) FROM BookReview br WHERE br.bookId = :bookId")
    Long getReviewCountForBook(@Param("bookId") String bookId);
    
    @Query("SELECT br FROM BookReview br WHERE br.reviewText LIKE %:keyword% OR br.reviewTitle LIKE %:keyword%")
    List<BookReview> findByReviewContainingKeyword(@Param("keyword") String keyword);
    
    Boolean existsByUserIdAndBookId(String userId, String bookId);
}
