package com.cybersecurity.sechamp2025.services;

import com.cybersecurity.sechamp2025.dto.UserBookWithQuantityDto;
import com.cybersecurity.sechamp2025.models.UserBook;
import com.cybersecurity.sechamp2025.repositories.UserBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserBookService {

    @Autowired
    private UserBookRepository userBookRepository;

    public List<UserBook> getUserBooks(String userId) {
        return userBookRepository.findByUserIdWithBooks(userId);
    }

    public boolean userOwnsBook(String userId, String bookId) {
        return userBookRepository.existsByUserIdAndBookId(userId, bookId);
    }

    public UserBook addBookToUser(String userId, String bookId, BigDecimal purchasePrice) {
        // Check if user already owns this book
        if (userOwnsBook(userId, bookId)) {
            throw new IllegalStateException("User already owns this book");
        }

        String id = UUID.randomUUID().toString().substring(0, 12);
        UserBook userBook = new UserBook(id, userId, bookId, purchasePrice);
        return userBookRepository.save(userBook);
    }

    public UserBook addBookCopyToUser(String userId, String bookId, BigDecimal purchasePrice) {
        // Allow purchasing multiple copies of the same book
        String id = UUID.randomUUID().toString().substring(0, 12);
        UserBook userBook = new UserBook(id, userId, bookId, purchasePrice);
        return userBookRepository.save(userBook);
    }

    public Optional<UserBook> getUserBook(String userId, String bookId) {
        return userBookRepository.findByUserIdAndBookId(userId, bookId);
    }

    public List<UserBook> getAllUserBooks(String userId) {
        return userBookRepository.findByUserId(userId);
    }

    public List<UserBookWithQuantityDto> getUserBooksWithQuantities(String userId) {
        List<UserBook> userBooks = userBookRepository.findByUserIdWithBooks(userId);
        
        // Group by book ID and count quantities
        Map<String, List<UserBook>> bookGroups = userBooks.stream()
            .collect(Collectors.groupingBy(UserBook::getBookId));
        
        List<UserBookWithQuantityDto> result = new ArrayList<>();
        
        for (Map.Entry<String, List<UserBook>> entry : bookGroups.entrySet()) {
            List<UserBook> purchases = entry.getValue();
            UserBook firstPurchase = purchases.get(0);
            
            // Calculate stats for this book
            int quantity = purchases.size();
            LocalDateTime firstPurchaseDate = purchases.stream()
                .map(UserBook::getPurchaseDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);
            LocalDateTime lastPurchaseDate = purchases.stream()
                .map(UserBook::getPurchaseDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            BigDecimal totalSpent = purchases.stream()
                .map(UserBook::getPurchasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            UserBookWithQuantityDto dto = new UserBookWithQuantityDto(
                firstPurchase.getBook(),
                quantity,
                firstPurchaseDate,
                lastPurchaseDate,
                totalSpent
            );
            
            result.add(dto);
        }
        
        return result;
    }
}
