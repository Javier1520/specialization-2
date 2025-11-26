package com.epam.gym.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        log.info("Login attempt for username: {}", username);
        authenticationService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for username: {}", request.username());
        authenticationService.changePassword(request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}

