package com.epam.gym.service;

import java.util.function.Predicate;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class UsernamePasswordGenerator {
    private static final int CHARACTER_AMOUNT = 10;

    public String generateUsername(
            String firstName, String lastName, Predicate<String> usernameExists) {
        String base = String.format("%s.%s", firstName, lastName);
        String candidate = base;
        int suffix = 1;
        while (usernameExists.test(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    public String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(CHARACTER_AMOUNT);
    }
}
