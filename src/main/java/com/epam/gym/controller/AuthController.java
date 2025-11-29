package com.epam.gym.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.epam.gym.openapi.annotation.operation.GetByIdOperation;
import com.epam.gym.openapi.annotation.operation.UpdateOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Authentication", description = "Authentication Operations")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationService authenticationService;
    private final LogUtils logUtils;

    @GetByIdOperation(summary = "Login", description = "Authenticate User")
    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        logUtils.info(log, "Login attempt for username: {}", username);
        authenticationService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @UpdateOperation(summary = "Change Password", description = "Change User Password")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logUtils.info(log, "Change password request for username: {}", request.username());
        authenticationService.changePassword(request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}

