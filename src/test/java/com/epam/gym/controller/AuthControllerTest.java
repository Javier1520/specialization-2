package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private LogUtils logUtils;

    @InjectMocks
    private AuthController authController;

    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        changePasswordRequest = new ChangePasswordRequest("testuser", "oldPass123",
                "newPass123");
    }

    @Test
    void login_success_returnsOk() {
        // Given
        String username = "testuser";
        String password = "password123";
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = new LoginResponse("jwt-token", "refresh-token");

        when(authenticationService.authenticate(username, password)).thenReturn(loginResponse);

        // When
        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authenticationService).authenticate(eq(username), eq(password));
    }

    @Test
    void login_invalidCredentials_throwsException() {
        // Given
        String username = "testuser";
        String password = "wrongpass";
        LoginRequest loginRequest = new LoginRequest(username, password);

        doThrow(new ValidationException("Invalid credentials"))
                .when(authenticationService).authenticate(username, password);

        // When & Then
        try {
            authController.login(loginRequest);
        } catch (ValidationException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
        verify(authenticationService).authenticate(eq(username), eq(password));
    }

    @Test
    void refresh_success_returnsOk() {
        // Given
        String refreshToken = "refresh-token";
        com.epam.gym.dto.request.RefreshTokenRequest request = new com.epam.gym.dto.request.RefreshTokenRequest(refreshToken);
        LoginResponse loginResponse = new LoginResponse("new-jwt-token", refreshToken);

        when(authenticationService.refreshToken(refreshToken)).thenReturn(loginResponse);

        // When
        ResponseEntity<LoginResponse> response = authController.refresh(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authenticationService).refreshToken(refreshToken);
    }

    @Test
    void logout_success_returnsOk() {
        // Given
        String refreshToken = "refresh-token";
        com.epam.gym.dto.request.RefreshTokenRequest request = new com.epam.gym.dto.request.RefreshTokenRequest(refreshToken);

        doNothing().when(authenticationService).logout(refreshToken);

        // When
        ResponseEntity<Void> response = authController.logout(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).logout(refreshToken);
    }

    @Test
    void changePassword_success_returnsOk() {
        // Given
        doNothing().when(authenticationService).changePassword(
                changePasswordRequest.username(),
                changePasswordRequest.oldPassword(),
                changePasswordRequest.newPassword()
        );

        // When
        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).changePassword(
                eq(changePasswordRequest.username()),
                eq(changePasswordRequest.oldPassword()),
                eq(changePasswordRequest.newPassword())
        );
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void changePassword_userNotFound_throwsException() {
        // Given
        doThrow(new NotFoundException("User not found"))
                .when(authenticationService).changePassword(
                        changePasswordRequest.username(),
                        changePasswordRequest.oldPassword(),
                        changePasswordRequest.newPassword()
                );

        // When & Then
        try {
            authController.changePassword(changePasswordRequest);
        } catch (NotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }
        verify(authenticationService).changePassword(
                eq(changePasswordRequest.username()),
                eq(changePasswordRequest.oldPassword()),
                eq(changePasswordRequest.newPassword())
        );
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        // Given
        doThrow(new ValidationException("Old password is incorrect"))
                .when(authenticationService).changePassword(
                        changePasswordRequest.username(),
                        changePasswordRequest.oldPassword(),
                        changePasswordRequest.newPassword()
                );

        // When & Then
        try {
            authController.changePassword(changePasswordRequest);
        } catch (ValidationException e) {
            assertEquals("Old password is incorrect", e.getMessage());
        }
        verify(authenticationService).changePassword(
                eq(changePasswordRequest.username()),
                eq(changePasswordRequest.oldPassword()),
                eq(changePasswordRequest.newPassword())
        );
    }
}

