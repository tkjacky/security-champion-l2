package com.cybersecurity.sechamp2025.services;

import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(String id) {
        return bookRepository.findById(id).orElse(null);
    }

    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category);
    }

    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    public List<String> getCategories() {
        return bookRepository.findDistinctCategories();
    }

    public List<Book> getFeaturedBooks() {
        return bookRepository.findByRatingGreaterThanEqual(BigDecimal.valueOf(4.5));
    }
}
