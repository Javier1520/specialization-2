package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.RefreshTokenRequest;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.openapi.annotation.operation.CreateOperation;
import com.epam.gym.openapi.annotation.operation.UpdateOperation;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Authentication Operations")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final LogUtils logUtils;

    public AuthController(AuthenticationService authenticationService, LogUtils logUtils) {
        this.authenticationService = authenticationService;
        this.logUtils = logUtils;
    }

    @CreateOperation(summary = "Login", description = "Authenticate User and Get JWT Token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logUtils.info(log, "Login attempt for username: {}", request.username());
        LoginResponse response =
                authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        logUtils.info(log, "Refresh token request");
        LoginResponse response = authenticationService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logUtils.info(log, "Logout request");
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @UpdateOperation(summary = "Change Password", description = "Change User Password")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logUtils.info(log, "Change password request for username: {}", request.username());
        authenticationService.changePassword(
                request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
