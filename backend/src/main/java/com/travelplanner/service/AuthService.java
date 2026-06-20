package com.travelplanner.service;

import com.mongodb.MongoWriteException;
import com.travelplanner.dto.request.SigninRequestDto;
import com.travelplanner.dto.request.SignupRequestDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.exception.UnauthorizedException;
import com.travelplanner.model.User;
import com.travelplanner.repository.UserRepository;
import com.travelplanner.util.PasswordHasher;
import com.travelplanner.util.TokenService;

import java.util.Locale;

public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public AuthResult signup(SignupRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (email.isEmpty() || request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("A valid email and a password of at least 6 characters are required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("An account with that email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(request.getName());
        user.setPasswordHash(PasswordHasher.hash(request.getPassword()));
        user.setCreatedAt(System.currentTimeMillis());
        try {
            userRepository.insert(user);
        } catch (MongoWriteException e) {
            // Unique-index race: two signups for the same email landed concurrently.
            throw new BadRequestException("An account with that email already exists");
        }

        return new AuthResult(user, tokenService.issue(user.getId(), user.getEmail()));
    }

    public AuthResult signin(SigninRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (request.getPassword() == null || !PasswordHasher.verify(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return new AuthResult(user, tokenService.issue(user.getId(), user.getEmail()));
    }

    /** Resolves the calling user from an "Authorization: Bearer &lt;token&gt;" header value. */
    public User resolveUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or malformed Authorization header");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        TokenService.Claims claims;
        try {
            claims = tokenService.verify(token);
        } catch (SecurityException e) {
            throw new UnauthorizedException(e.getMessage());
        }
        return userRepository.findById(claims.userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static final class AuthResult {
        private final User user;
        private final String token;

        public AuthResult(User user, String token) {
            this.user = user;
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}
