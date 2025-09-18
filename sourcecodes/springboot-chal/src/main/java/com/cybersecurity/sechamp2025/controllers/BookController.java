package com.cybersecurity.sechamp2025.controllers;

import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/books")
    public String booksPage(Model model, 
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) String search) {
        
        List<Book> books;
        String pageTitle = "All Books";
        
        if (category != null && !category.trim().isEmpty()) {
            books = bookService.findByCategory(category);
            pageTitle = category + " Books";
        } else if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchByTitle(search);
            pageTitle = "Search Results for: " + search;
        } else {
            books = bookService.findAll();
        }
        
        List<String> categories = bookService.getCategories();
        List<Book> featuredBooks = bookService.getFeaturedBooks();
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("featuredBooks", featuredBooks);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentCategory", category);
        model.addAttribute("searchQuery", search);
        
        return "books";
    }
}
