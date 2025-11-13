package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataIDMapperTest {
    private DataIDMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DataIDMapper();
    }

    @Test
    @DisplayName("Should store and retrieve mappings correctly")
    void addAndGetMapping_ShouldWorkCorrectly() {
        // Arrange & Act
        mapper.addMapping("Trainer", 1L, 100L);
        mapper.addMapping("Trainee", 1L, 200L);
        mapper.addMapping("Trainer", 2L, 101L);

        // Assert
        assertEquals(100L, mapper.getMappedId("Trainer", 1L));
        assertEquals(200L, mapper.getMappedId("Trainee", 1L));
        assertEquals(101L, mapper.getMappedId("Trainer", 2L));
    }

    @Test
    @DisplayName("Should return original ID when mapping not found")
    void getMappedId_WhenMappingNotFound_ShouldReturnOriginalId() {
        // Arrange
        mapper.addMapping("Trainer", 1L, 100L);

        // Act & Assert
        assertEquals(2L, mapper.getMappedId("Trainer", 2L));
        assertEquals(1L, mapper.getMappedId("Trainee", 1L));
    }

    @Test
    @DisplayName("Should handle multiple mappings for different types")
    void addMapping_MultipleMappings_ShouldHandleCorrectly() {
        // Arrange & Act
        mapper.addMapping("Trainer", 1L, 100L);
        mapper.addMapping("Trainee", 1L, 200L);
        mapper.addMapping("Training", 1L, 300L);
        mapper.addMapping("TrainingType", 1L, 400L);

        // Assert
        assertEquals(100L, mapper.getMappedId("Trainer", 1L));
        assertEquals(200L, mapper.getMappedId("Trainee", 1L));
        assertEquals(300L, mapper.getMappedId("Training", 1L));
        assertEquals(400L, mapper.getMappedId("TrainingType", 1L));
    }

    @Test
    @DisplayName("Should handle update of existing mapping")
    void addMapping_UpdateExisting_ShouldOverwritePreviousMapping() {
        // Arrange
        mapper.addMapping("Trainer", 1L, 100L);

        // Act
        mapper.addMapping("Trainer", 1L, 200L);

        // Assert
        assertEquals(200L, mapper.getMappedId("Trainer", 1L));
    }

    @Test
    @DisplayName("Should handle non-existent type")
    void getMappedId_NonExistentType_ShouldReturnOriginalId() {
        // Arrange & Act & Assert
        assertEquals(1L, mapper.getMappedId("NonExistentType", 1L));
    }
}