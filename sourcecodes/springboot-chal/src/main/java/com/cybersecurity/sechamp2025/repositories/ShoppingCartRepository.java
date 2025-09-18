package com.cybersecurity.sechamp2025.repositories;

import com.cybersecurity.sechamp2025.models.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, String> {
    
    Optional<ShoppingCart> findByUserIdAndStatus(String userId, String status);
    
    List<ShoppingCart> findByUserIdAndStatusIn(String userId, List<String> statuses);
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.expiresAt < :now AND sc.status IN ('ACTIVE', 'CHECKING_OUT')")
    List<ShoppingCart> findExpiredCarts(@Param("now") Instant now);
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.userId = :userId AND sc.status = 'ACTIVE'")
    Optional<ShoppingCart> findActiveCartByUserId(@Param("userId") String userId);
}
