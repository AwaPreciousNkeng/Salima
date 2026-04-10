package com.codewithpcodes.salima.auth;

import com.codewithpcodes.salima.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        service.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<AuthenticationResponse> createAdmin(
            @Valid @RequestBody CreateAdminRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.createAdmin(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @PatchMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgottenPasswordRequest request
    ) {
        service.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest request
    ) {
        service.verifyResetCode(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        service.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
