package com.epam.gym.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TransactionIdGeneratorTest {

    @Test
    void generate_returnsValidUUID() {
        // When
        String result = TransactionIdGenerator.generate();

        // Then
        assertNotNull(result);
        // Verify it's a valid UUID format
        UUID.fromString(result); // Will throw if invalid
    }

    @Test
    void generate_returnsUniqueValues() {
        // Given
        Set<String> generatedIds = new HashSet<>();
        int iterations = 100;

        // When
        for (int i = 0; i < iterations; i++) {
            String id = TransactionIdGenerator.generate();
            generatedIds.add(id);
        }

        // Then
        assertEquals(iterations, generatedIds.size(), "All generated IDs should be unique");
    }

    @Test
    void generate_returnsNonEmptyString() {
        // When
        String result = TransactionIdGenerator.generate();

        // Then
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void generate_multipleCalls_returnDifferentValues() {
        // When
        String id1 = TransactionIdGenerator.generate();
        String id2 = TransactionIdGenerator.generate();
        String id3 = TransactionIdGenerator.generate();

        // Then
        assertNotSame(id1, id2);
        assertNotSame(id2, id3);
        assertNotSame(id1, id3);
    }

    @Test
    void generate_returnsUUIDFormat() {
        // When
        String result = TransactionIdGenerator.generate();

        // Then
        assertNotNull(result);
        // UUID format: 8-4-4-4-12 hexadecimal digits
        assertTrue(result.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
}

