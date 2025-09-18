package com.cybersecurity.sechamp2025.services;

import com.cybersecurity.sechamp2025.models.BookReview;
import com.cybersecurity.sechamp2025.repositories.BookReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BookReviewService {

    @Autowired
    private BookReviewRepository bookReviewRepository;

    public List<BookReview> findAll() {
        return bookReviewRepository.findAll();
    }

    public BookReview findById(String id) {
        return bookReviewRepository.findById(id).orElse(null);
    }

    public List<BookReview> findByBookId(String bookId) {
        return bookReviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public List<BookReview> findByUserId(String userId) {
        return bookReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<BookReview> findByRatingGreaterThanEqual(BigDecimal rating) {
        return bookReviewRepository.findByRatingGreaterThanEqualOrderByCreatedAtDesc(rating);
    }

    public List<BookReview> findVerifiedPurchaseReviews() {
        return bookReviewRepository.findByVerifiedPurchaseTrueOrderByHelpfulCountDesc();
    }

    public List<BookReview> searchByKeyword(String keyword) {
        return bookReviewRepository.findByReviewContainingKeyword(keyword);
    }

    public BigDecimal getAverageRatingForBook(String bookId) {
        BigDecimal avgRating = bookReviewRepository.getAverageRatingForBook(bookId);
        return avgRating != null ? avgRating : BigDecimal.ZERO;
    }

    public Long getReviewCountForBook(String bookId) {
        return bookReviewRepository.getReviewCountForBook(bookId);
    }

    public Boolean hasUserReviewedBook(String userId, String bookId) {
        return bookReviewRepository.existsByUserIdAndBookId(userId, bookId);
    }

    public BookReview save(BookReview bookReview) {
        if (bookReview.getId() == null || bookReview.getId().trim().isEmpty()) {
            bookReview.setId(UUID.randomUUID().toString());
        }
        return bookReviewRepository.save(bookReview);
    }

    public BookReview createReview(String bookId, String userId, BigDecimal rating, 
                                  String reviewTitle, String reviewText, Boolean verifiedPurchase) {
        // Check if user has already reviewed this book
        if (hasUserReviewedBook(userId, bookId)) {
            throw new IllegalStateException("User has already reviewed this book");
        }
        
        BookReview review = new BookReview(
            UUID.randomUUID().toString(),
            bookId,
            userId,
            rating,
            reviewTitle,
            reviewText,
            verifiedPurchase
        );
        
        return save(review);
    }

    public void deleteById(String id) {
        bookReviewRepository.deleteById(id);
    }

    public BookReview incrementHelpfulCount(String reviewId) {
        BookReview review = findById(reviewId);
        if (review != null) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            return save(review);
        }
        return null;
    }
}
