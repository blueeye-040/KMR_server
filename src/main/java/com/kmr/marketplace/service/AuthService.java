package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.AuthResponse;
import com.kmr.marketplace.dto.LoginRequest;
import com.kmr.marketplace.security.OtpStore;
import com.kmr.marketplace.dto.RegisterRequest;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.entity.UserRole;
import com.kmr.marketplace.repository.UserRepository;
import com.kmr.marketplace.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpStore otpStore;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       OtpStore otpStore,
                       EmailService emailService,
                       SmsService smsService) {
        this.userRepository      = userRepository;
        this.passwordEncoder     = passwordEncoder;
        this.jwtService          = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpStore = otpStore;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    private String newCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    // ── Email confirmation (alternative to phone OTP) ───────────────────────────

    /** Send a 6-digit confirmation code to an email during sign-up. */
    public void sendEmailOtp(String email) {
        String code = newCode();
        otpStore.saveOtp(email, code);
        emailService.sendVerificationCode(email, code);
    }

    public void verifyEmailOtp(String email, String otp) {
        otpStore.verifyOtp(email, otp);   // throws on wrong/expired code
    }

    // ── Registration ────────────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (request.phone() != null && !request.phone().isBlank()
                && userRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        // Account confirmation: the user proves ownership of EITHER their phone
        // (SMS OTP) OR their email (email code) before the account is created.
        boolean phoneVerified = request.phone() != null && !request.phone().isBlank()
                && otpStore.isOtpVerified(request.phone());
        boolean emailVerified = otpStore.isOtpVerified(request.email());
        if (!phoneVerified && !emailVerified) {
            throw new IllegalArgumentException(
                    "Please confirm your account by verifying your email or phone first.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);
        otpStore.remove(request.email());
        if (request.phone() != null) otpStore.remove(request.phone());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    // ── Forgot / reset password ─────────────────────────────────────────────────

    private static final String RESET_PREFIX = "reset:";

    /**
     * Send a reset code to the account's email or phone. To avoid leaking which
     * accounts exist, the caller always gets the same response — a code is only
     * actually sent when a matching account is found.
     */
    public void forgotPassword(String identifier) {
        String id = identifier.trim();
        boolean isEmail = id.contains("@");
        User user = (isEmail ? userRepository.findByEmail(id)
                             : userRepository.findByPhone(id)).orElse(null);
        if (user == null) return;   // silent — don't reveal existence

        String code = newCode();
        otpStore.saveOtp(RESET_PREFIX + id, code);
        if (isEmail) {
            emailService.sendPasswordResetCode(user.getEmail(), code);
        } else if (user.getPhone() != null) {
            smsService.sendText(user.getPhone(),
                    "Your Valley Rush password reset code is: " + code
                            + ". Valid for 5 minutes. Do not share.");
        }
    }

    public void resetPassword(String identifier, String otp, String newPassword) {
        String id = identifier.trim();
        otpStore.verifyOtp(RESET_PREFIX + id, otp);   // throws on wrong/expired
        User user = (id.contains("@") ? userRepository.findByEmail(id)
                                      : userRepository.findByPhone(id))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpStore.remove(RESET_PREFIX + id);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}
