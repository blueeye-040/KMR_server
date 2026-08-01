package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.UpdateProfileRequest;
import com.kmr.marketplace.dto.UserDto;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.UserRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepo;
    private final AuthHelper authHelper;

    public ProfileService(UserRepository userRepo, AuthHelper authHelper) {
        this.userRepo   = userRepo;
        this.authHelper = authHelper;
    }

    @Transactional(readOnly = true)
    public UserDto me() {
        return toDto(authHelper.currentUser());
    }

    public UserDto update(UpdateProfileRequest req) {
        User user = authHelper.currentUser();
        user.setName(req.name().trim());
        if (req.phone() != null && !req.phone().isBlank()) user.setPhone(req.phone().trim());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        return toDto(userRepo.save(user));
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(),
                u.getPhone(), u.getAvatarUrl(), u.getRole().name());
    }
}
