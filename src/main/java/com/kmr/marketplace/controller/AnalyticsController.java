package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.AnalyticsResponse;
import com.kmr.marketplace.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Requires auth — JWT filter enforces this
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> summary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
