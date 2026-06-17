package com.kmr.marketplace.security;

import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thin helper for working with the authenticated user inside services/controllers.
 *
 * Usage in a controller:
 *   User me = authHelper.currentUser();
 *   authHelper.requireRole(UserRole.ADMIN);
 */
@Component
public class AuthHelper {

    /** Returns the currently authenticated user, or null if the request is anonymous. */
    public User currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof User u ? u : null;
    }

    /** Returns the currently authenticated user. Throws 401 if unauthenticated. */
    public User currentUser() {
        User user = currentUserOrNull();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    /** Throws 403 if the current user does not have the required role. */
    public void requireRole(UserRole required) {
        User user = currentUser();
        if (user.getRole() != required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Required role: " + required.name());
        }
    }

    /** Returns true if there is an authenticated user with the given role. */
    public boolean hasRole(UserRole role) {
        User user = currentUserOrNull();
        return user != null && user.getRole() == role;
    }
}
