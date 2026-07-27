package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.UpdateProfileRequest;
import com.kmr.marketplace.dto.UserDto;
import com.kmr.marketplace.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserDto me() {
        return profileService.me();
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateProfileRequest req) {
        return profileService.update(req);
    }
}
