package com.cybersecurity.sechamp2025.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShoppingCartController {
    
    @GetMapping("/cart")
    public String showCart() {
        return "cart";
    }
    
    @GetMapping("/checkout")
    public String showCheckoutConfirmation() {
        return "checkout-confirmation";
    }
}
