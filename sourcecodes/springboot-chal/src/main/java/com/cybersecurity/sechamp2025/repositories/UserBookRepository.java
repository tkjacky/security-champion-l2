package com.cybersecurity.sechamp2025.repositories;

import com.cybersecurity.sechamp2025.models.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, String> {
    
    List<UserBook> findByUserId(String userId);
    
    List<UserBook> findByBookId(String bookId);
    
    Optional<UserBook> findByUserIdAndBookId(String userId, String bookId);
    
    @Query("SELECT ub FROM UserBook ub JOIN FETCH ub.book WHERE ub.userId = :userId ORDER BY ub.purchaseDate DESC")
    List<UserBook> findByUserIdWithBooks(@Param("userId") String userId);
    
    boolean existsByUserIdAndBookId(String userId, String bookId);
}
