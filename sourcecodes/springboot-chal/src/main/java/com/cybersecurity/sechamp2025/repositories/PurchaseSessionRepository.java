package com.cybersecurity.sechamp2025.repositories;

import com.cybersecurity.sechamp2025.models.PurchaseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseSessionRepository extends JpaRepository<PurchaseSession, String> {
    
    Optional<PurchaseSession> findByUserIdAndBookIdAndStatus(String userId, String bookId, String status);
    
    List<PurchaseSession> findByUserIdAndStatus(String userId, String status);
    
    List<PurchaseSession> findByBookIdAndStatus(String bookId, String status);
    
    @Query("SELECT ps FROM PurchaseSession ps WHERE ps.expiresAt < :currentTime AND ps.status = 'LOCKED'")
    List<PurchaseSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    void deleteByUserIdAndBookId(String userId, String bookId);
}
