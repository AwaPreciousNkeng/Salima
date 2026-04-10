package com.codewithpcodes.salima.auth;

import com.codewithpcodes.salima.notification.EmailNotificationRequest;
import com.codewithpcodes.salima.notification.EmailTemplate;
import com.codewithpcodes.salima.notification.NotificationService;
import com.codewithpcodes.salima.config.JwtService;
import com.codewithpcodes.salima.token.Token;
import com.codewithpcodes.salima.token.TokenRepository;
import com.codewithpcodes.salima.token.TokenType;
import com.codewithpcodes.salima.user.Role;
import com.codewithpcodes.salima.user.User;
import com.codewithpcodes.salima.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public void register(RegistrationRequest request) {

        String defaultProfilePicture = "https://ui-avatars.com/api?name=" +
                URLEncoder.encode(request.firstName() + " " + request.lastName(), StandardCharsets.UTF_8) +
                "&background=random&color=fff%size=256";

        //1. Check for duplicate email
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed. Email {} already in use", request.email());
            throw new IllegalArgumentException("User already exists with email: " + request.email());
        }

        //2. Build and save the user
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .profilePicturePath(defaultProfilePicture)
                .nationalId(request.nationalId())
                .gender(request.gender())
                .phoneNumber(request.phoneNumber())
                .street(request.street())
                .city(request.city())
                .region(request.region())
                .dateOfBirth(request.dateOfBirth())
                .isVerified(true)
                .build();

        User savedUser = userRepository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //1. Authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new RuntimeException("Invalid credentials: " + e);
        }

        //2. Retrieve user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        //3. Issue new token
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        //4. Revoke old and save new token
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        log.info("User {} logged in successfully", user.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String accessToken) {
        Token token = Token.builder()
                .user(user)
                .token(accessToken)
                .type(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .gender(user.getGender())
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public AuthenticationResponse createAdmin(CreateAdminRequest request) {
        String defaultProfilePicture = "https://ui-avatars.com/api?name=" +
                URLEncoder.encode(request.firstName() + " " + request.lastName(), StandardCharsets.UTF_8) +
                "&background=random&color=fff%size=256";

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User already exists with email: " + request.email());
        }

        User admin = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .dateOfBirth(request.dateOfBirth())
                .role(Role.ADMIN)
                .profilePicturePath(defaultProfilePicture)
                .isVerified(true)
                .build();

        User savedAdmin = userRepository.save(admin);

        String accessToken = jwtService.generateToken(savedAdmin);
        String refreshToken = jwtService.generateRefreshToken(savedAdmin);
        saveUserToken(savedAdmin, accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedAdmin.getId())
                .fullName(savedAdmin.getFullName())
                .gender(savedAdmin.getGender())
                .build();
    }

    public void forgotPassword(ForgottenPasswordRequest request) {
        //1. Check the user exists
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        // 2. Generate a secure, short-lived confirmation code
        String confirmationCode = String.format("%06d", new Random().nextInt(1000000));

        // 3. Save the code and expiry time
        user.setResetPasswordCode(confirmationCode);
        user.setResetPasswordCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // 4. Send it to the user's email using the template
        EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                .to(user.getEmail())
                .subject(EmailTemplate.RESET_PASSWORD.getSubject())
                .templateName(EmailTemplate.RESET_PASSWORD.getTemplate())
                .templateModel(Map.of(
                        "username", user.getFullName(),
                        "confirmationCode", confirmationCode
                ))
                .build();

        notificationService.sendEmail(emailRequest);
        log.info("Password reset email initiated for user {}", user.getEmail());
    }

    public void verifyResetCode(VerifyResetCodeRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        if (user.getResetPasswordCode() == null || !user.getResetPasswordCode().equals(request.code())) {
            throw new IllegalArgumentException("Invalid confirmation code");
        }

        if (user.getResetPasswordCodeExpiry() == null || user.getResetPasswordCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Confirmation code has expired");
        }
        
        log.info("Reset code verified for user {}", user.getEmail());
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        // 1. Verify the code again to be sure
        verifyResetCode(new VerifyResetCodeRequest(request.email(), request.code()));

        // 2. Validate passwords match
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 3. Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        
        // 4. Clear the reset code
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiry(null);
        
        userRepository.save(user);

        // 5. Revoke all tokens
        revokeAllUserTokens(user);
        
        log.info("Password reset successfully for user {}", user.getEmail());
    }

}
