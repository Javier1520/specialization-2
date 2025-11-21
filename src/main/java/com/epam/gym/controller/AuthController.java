package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password) {
        log.info("Login attempt for username: {}", username);
        authenticationService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for username: {}", request.getUsername());
        authenticationService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}

