package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.CreateTicketRequest;
import com.kmr.marketplace.dto.TicketDto;
import com.kmr.marketplace.service.SupportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDto create(@Valid @RequestBody CreateTicketRequest req) {
        return supportService.create(req);
    }

    @GetMapping
    public List<TicketDto> list() {
        return supportService.list();
    }

    @GetMapping("/{id}")
    public TicketDto get(@PathVariable Long id) {
        return supportService.get(id);
    }
}
