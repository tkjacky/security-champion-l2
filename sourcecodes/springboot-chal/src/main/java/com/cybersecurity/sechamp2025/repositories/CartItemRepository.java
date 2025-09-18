package com.cybersecurity.sechamp2025.repositories;

import com.cybersecurity.sechamp2025.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    List<CartItem> findByShoppingCartId(String cartId);
    
    Optional<CartItem> findByShoppingCartIdAndBookId(String cartId, String bookId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.reservedUntil < :now")
    List<CartItem> findExpiredReservations(@Param("now") Instant now);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.bookId = :bookId AND ci.reservedUntil > :now")
    List<CartItem> findActiveReservationsForBook(@Param("bookId") String bookId, @Param("now") Instant now);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.bookId = :bookId AND ci.reservedUntil > :now")
    long countActiveReservationsForBook(@Param("bookId") String bookId, @Param("now") Instant now);
}
