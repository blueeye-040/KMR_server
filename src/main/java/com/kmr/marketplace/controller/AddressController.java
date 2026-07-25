package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.AddressDto;
import com.kmr.marketplace.dto.AddressRequest;
import com.kmr.marketplace.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressDto> list() {
        return addressService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto create(@Valid @RequestBody AddressRequest req) {
        return addressService.create(req);
    }

    @PutMapping("/{id}")
    public AddressDto update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return addressService.update(id, req);
    }

    @PutMapping("/{id}/default")
    public AddressDto setDefault(@PathVariable Long id) {
        return addressService.setDefault(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}
