package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.CategoryDto;
import com.kmr.marketplace.dto.HomeResponse;
import com.kmr.marketplace.service.HomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    // Public — no auth required for browsing
    @GetMapping("/home")
    public ResponseEntity<HomeResponse> home() {
        return ResponseEntity.ok(homeService.getHomeFeed());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> categories() {
        return ResponseEntity.ok(homeService.getCategories());
    }
}
