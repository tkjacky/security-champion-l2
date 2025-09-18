package com.cybersecurity.sechamp2025.controllers;

import com.cybersecurity.sechamp2025.models.Book;
import com.cybersecurity.sechamp2025.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserListController {

    @Autowired
    private BookService bookService;

    @GetMapping("/users")
    public String usersPage(Model model) {
        // Get currently reading books (for demo, we'll get some featured books)
        List<Book> currentlyReading = bookService.getFeaturedBooks();
        
        // Get recommended books (random selection from all books)
        List<Book> allBooks = bookService.findAll();
        List<Book> recommendedBooks = allBooks.size() > 6 ? allBooks.subList(0, 6) : allBooks;
        
        // Get recent activity books (another selection)
        List<Book> recentActivityBooks = allBooks.size() > 4 ? allBooks.subList(allBooks.size() - 4, allBooks.size()) : allBooks;
        
        model.addAttribute("currentlyReading", currentlyReading);
        model.addAttribute("recommendedBooks", recommendedBooks);
        model.addAttribute("recentActivityBooks", recentActivityBooks);
        
        return "users"; // JS will still handle user data via /api/users
    }
}
