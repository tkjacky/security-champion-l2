package com.cybersecurity.sechamp2025.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.models.PurchaseSession;
import com.cybersecurity.sechamp2025.repositories.PurchaseSessionRepository;

@Service
public class PurchaseSessionService {

    @Autowired
    private PurchaseSessionRepository purchaseSessionRepository;
    
    @Autowired
    private BookService bookService;

    @Transactional
    public PurchaseSession createSession(String userId, String bookId) {
        // Check if there's already an active session for this user and book
        Optional<PurchaseSession> existingSession = purchaseSessionRepository
            .findByUserIdAndBookIdAndStatus(userId, bookId, "LOCKED");
        
        if (existingSession.isPresent() && !existingSession.get().isExpired()) {
            return existingSession.get(); // Return existing session if not expired
        }
        
        // Create new session
        String sessionId = UUID.randomUUID().toString().substring(0, 12);
        PurchaseSession session = new PurchaseSession(sessionId, userId, bookId);
        
        // Remove any existing session for this user and book
        if (existingSession.isPresent()) {
            purchaseSessionRepository.delete(existingSession.get());
        }
        
        return purchaseSessionRepository.save(session);
    }

    public Optional<PurchaseSession> getActiveSession(String userId, String bookId) {
        Optional<PurchaseSession> session = purchaseSessionRepository
            .findByUserIdAndBookIdAndStatus(userId, bookId, "LOCKED");
        
        if (session.isPresent() && session.get().isExpired()) {
            // Session expired, remove it
            purchaseSessionRepository.delete(session.get());
            return Optional.empty();
        }
        
        return session;
    }

    public Optional<PurchaseSession> getSessionById(String sessionId) {
        return purchaseSessionRepository.findById(sessionId);
    }

    @Transactional
    public void completeSession(String sessionId) {
        Optional<PurchaseSession> session = purchaseSessionRepository.findById(sessionId);
        if (session.isPresent()) {
            PurchaseSession ps = session.get();
            ps.setStatus("COMPLETED");
            purchaseSessionRepository.save(ps);
        }
    }

    @Transactional
    public void cancelSession(String sessionId) {
        Optional<PurchaseSession> session = purchaseSessionRepository.findById(sessionId);
        if (session.isPresent()) {
            purchaseSessionRepository.delete(session.get());
        }
    }

    public List<PurchaseSession> getLockedSessionsForBook(String bookId) {
        return purchaseSessionRepository.findByBookIdAndStatus(bookId, "LOCKED")
            .stream()
            .filter(session -> !session.isExpired())
            .toList();
    }

    @Transactional
    public void cleanupExpiredSessions() {
        List<PurchaseSession> expiredSessions = purchaseSessionRepository
            .findExpiredSessions(LocalDateTime.now());
        purchaseSessionRepository.deleteAll(expiredSessions);
    }

    // RACE CONDITION: Non-atomic stock decrement allowing negative stock
    public boolean purchaseBook(String bookId) {
        Book book = bookService.findById(bookId);
        book.setStock(book.getStock() - 1);
        bookService.save(book);
        return true;
    }
}
