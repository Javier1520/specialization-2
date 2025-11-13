package com.epam.gym.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UsernamePasswordGeneratorTest {

    private UsernamePasswordGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UsernamePasswordGenerator();
    }

    @Test
    void generateUsername_ShouldCreateBasicUsername_WhenNoConflict() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        Predicate<String> noConflicts = username -> false;

        // Act
        String username = generator.generateUsername(firstName, lastName, noConflicts);

        // Assert
        assertEquals("John.Doe", username);
    }

    @Test
    void generateUsername_ShouldAddNumber_WhenConflictExists() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("John.Doe");
        Predicate<String> hasConflict = existingUsernames::contains;

        // Act
        String username = generator.generateUsername(firstName, lastName, hasConflict);

        // Assert
        assertEquals("John.Doe1", username);
    }

    @Test
    void generateUsername_ShouldIncrementNumber_UntilNoConflict() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("John.Doe");
        existingUsernames.add("John.Doe1");
        existingUsernames.add("John.Doe2");
        Predicate<String> hasConflict = existingUsernames::contains;

        // Act
        String username = generator.generateUsername(firstName, lastName, hasConflict);

        // Assert
        assertEquals("John.Doe3", username);
    }


    @Test
    void generateUsername_ShouldThrowException_WhenPredicateIsNull() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> generator.generateUsername(firstName, lastName, null));
    }

    @Test
    void generatePassword_ShouldGenerateRandomAlphanumericPassword() {
        // Act
        String password = generator.generatePassword();

        // Assert
        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]+"), "Password should only contain alphanumeric characters");
    }

    @Test
    void generatePassword_ShouldGenerateUniquePasswords() {
        // Act
        String password1 = generator.generatePassword();
        String password2 = generator.generatePassword();

        // Assert
        assertNotEquals(password1, password2, "Generated passwords should be unique");
    }
}