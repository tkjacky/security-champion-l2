package com.cybersecurity.sechamp2025.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/bookstore-location")
public class StoreLocationController {

    @GetMapping
    public String storeLocationForm() {
        return "bookstore-location";
    }

    @PostMapping
    public String findStores(@RequestParam String location, Model model) {
        // Simulate store search results
        List<String> storeTemplates = List.of(
            "SecBooks Downtown - 123 Main St, %s - (555) 123-4567 - 0.5 miles",
            "SecBooks Mall Location - 456 Shopping Center, %s - (555) 234-5678 - 1.2 miles", 
            "SecBooks University - 789 Campus Blvd, %s - (555) 345-6789 - 2.1 miles",
            "SecBooks Suburban - 321 Elm Street, %s - (555) 456-7890 - 3.4 miles"
        );

        StringBuilder results = new StringBuilder();
        Random random = new Random();
        int numStores = random.nextInt(3) + 2; // 2-4 stores

        results.append("<div class='row'>");
        for (int i = 0; i < numStores; i++) {
            String storeInfo = String.format(storeTemplates.get(i), location);
            results.append("<div class='col-md-6 mb-3'>");
            results.append("<div class='card'>");
            results.append("<div class='card-body'>");
            results.append("<h6 class='card-title text-primary'>").append(storeInfo.split(" - ")[0]).append("</h6>");
            results.append("<p class='card-text'>");
            results.append("<strong>Address:</strong> ").append(storeInfo.split(" - ")[1]).append("<br>");
            results.append("<strong>Phone:</strong> ").append(storeInfo.split(" - ")[2]).append("<br>");
            results.append("<strong>Distance:</strong> ").append(storeInfo.split(" - ")[3]);
            results.append("</p>");
            results.append("<div class='btn-group' role='group'>");
            results.append("<button class='btn btn-primary btn-sm'>Get Directions</button>");
            results.append("<button class='btn btn-outline-secondary btn-sm'>Call Store</button>");
            results.append("</div>");
            results.append("</div></div></div>");
        }
        results.append("</div>");

        results.append("<div class='alert alert-info mt-3'>");
        results.append("<strong>Search performed for:</strong> ").append(location);
        results.append("<br><small class='text-muted'>Results are estimated based on your location</small>");
        results.append("</div>");

        model.addAttribute("storeResults", results.toString());
        model.addAttribute("location", location);
        return "bookstore-location";
    }
}
