package com.epam.gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.service.AuthenticationService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        changePasswordRequest = new ChangePasswordRequest("testuser", "oldPass123", "newPass123");
    }

    @Test
    void login_success_returnsOk() {
        // Given
        String username = "testuser";
        String password = "password123";
        doNothing().when(authenticationService).authenticate(username, password);

        // When
        ResponseEntity<Void> response = authController.login(username, password);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).authenticate(eq(username), eq(password));
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void login_invalidCredentials_throwsException() {
        // Given
        String username = "testuser";
        String password = "wrongpass";
        doThrow(new ValidationException("Invalid credentials"))
                .when(authenticationService).authenticate(username, password);

        // When & Then
        try {
            authController.login(username, password);
        } catch (ValidationException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
        verify(authenticationService).authenticate(eq(username), eq(password));
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

