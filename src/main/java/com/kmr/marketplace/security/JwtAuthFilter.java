package com.kmr.marketplace.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService blacklistService;

    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsServiceImpl userDetailsService,
                         TokenBlacklistService blacklistService) {
        this.jwtService        = jwtService;
        this.userDetailsService = userDetailsService;
        this.blacklistService  = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // Reject blacklisted tokens (logged-out sessions)
        if (blacklistService.isBlacklisted(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
            return;
        }

        final String email = jwtService.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ex) {
                // DB or token errors must not block public endpoints — log and continue
                logger.warn("JWT authentication failed: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
