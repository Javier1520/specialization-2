package com.epam.gym.service;

public interface AuthenticationService {

    String authenticate(String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);
}


