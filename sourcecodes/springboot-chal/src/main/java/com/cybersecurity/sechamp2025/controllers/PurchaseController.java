package com.cybersecurity.sechamp2025.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PurchaseController {

    @GetMapping("/purchase/confirm")
    public String purchaseConfirmation(@RequestParam(required = false) String sessionId, Model model) {
        if (sessionId != null) {
            model.addAttribute("sessionId", sessionId);
        }
        return "purchase-confirmation";
    }
}
