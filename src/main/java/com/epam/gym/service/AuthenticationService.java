package com.epam.gym.service;

import com.epam.gym.dto.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse authenticate(String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);

    LoginResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
